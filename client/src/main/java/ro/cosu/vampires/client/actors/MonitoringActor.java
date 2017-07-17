/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.client.actors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.SortedMap;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.client.monitoring.MetricsWindow;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.JobStatus;
import ro.cosu.vampires.server.values.jobs.metrics.Metric;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class MonitoringActor extends UntypedActor {
    private static final int MONITORING_INTERVAL_MILLIS = 1000;
    private final MetricRegistry metricRegistry;
    private final MetricsWindow metricsWindow = new MetricsWindow();
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private SortedMap<String, Gauge> gauges;

    MonitoringActor(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;

        // getting the gauges early
        gauges = metricRegistry.getGauges((name, metric) -> name.startsWith("cpu") || name.startsWith("network"));

        recordMetrics();
    }

    public static Props props(MetricRegistry registry) {

        return Props.create(MonitoringActor.class, registry);
    }

    @Override
    public void preStart() {
        getContext().system().scheduler().schedule(Duration.Zero(),
                Duration.create(MONITORING_INTERVAL_MILLIS, MILLISECONDS),
                this::recordMetrics,
                getContext().system().dispatcher());
    }

    private void recordMetrics() {
        ZonedDateTime now = ZonedDateTime.now();
        metricsWindow.add(now, gauges);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.debug("{} from {}", message, getSender());

        if (message instanceof Job) {
            Job job = (Job) message;
            if (JobStatus.EXECUTED.equals(job.status())) {
                log.info("id: {} -> info:{} duration: {}", job.id(), job.status(), job.result().duration());
                job = getJobWithMetrics(job);
                getSender().tell(job, getSelf());
            } else {
                log.error("received workload result not present");
            }
        } else if (message instanceof Metrics) {
            // the client will ask for metrics to send to the server at boot time
            log.debug("got metrics request ");
            ImmutableList<Metric> interval = metricsWindow.getInterval(
                    ZonedDateTime.now().minus(MONITORING_INTERVAL_MILLIS, ChronoUnit.MILLIS), ZonedDateTime.now());
            Metrics metrics = Metrics.builder().metadata(getHostMetrics()).metrics(interval).build();
            getSender().tell(metrics, getSelf());
        } else {
            log.error("received unknown type of message");
            unhandled(message);
        }
    }

    private Job getJobWithMetrics(Job job) {
        ImmutableList<Metric> metricsWindowInterval = metricsWindow.getInterval
                (job.result().trace().start(), job.result().trace().stop());
        ImmutableMap<String, String> hostValues = getHostMetrics();
        Metrics metrics = Metrics.builder().metadata(hostValues).metrics(metricsWindowInterval).build();
        return job.withHostMetrics(metrics);
    }

    private ImmutableMap<String, String> getHostMetrics() {
        SortedMap<String, Gauge> hostGauges = metricRegistry.getGauges(
                (name, metric) -> name.startsWith("host"));
        return MetricsWindow.convertGaugesToString(hostGauges);
    }
}
