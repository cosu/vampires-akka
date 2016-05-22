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

package ro.cosu.vampires.server.actors;

import com.google.common.collect.Maps;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Job;

public class StatsActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private MetricRegistry metricRegistry = new MetricRegistry();

    private Slf4jReporter reporter;
    private Map<String, ClientInfo> clientsInfo = Maps.newHashMap();
    private Map<String, ResourceInfo> resourcesInfo = Maps.newHashMap();

    public static Props props() {
        return Props.create(new Creator<StatsActor>() {

            @Override
            public StatsActor create() throws Exception {
                return new StatsActor();
            }
        });
    }

    @Override
    public void preStart() {
        reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger("ro.cosu.vampires.server.stats"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(5, TimeUnit.SECONDS);

    }

    @Override
    public void postStop() {
        reporter.report();
        reporter.stop();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Job) {
            process((Job) message);
        }
        if (message instanceof ResourceInfo) {
            process((ResourceInfo) message);
        }
        if (message instanceof ClientInfo) {
            process((ClientInfo) message);
        } else {
            unhandled(message);
        }
    }

    private void process(ClientInfo message) {
        clientsInfo.put(message.id(), message);

    }

    private void process(ResourceInfo message) {
        resourcesInfo.put(message.parameters().id(), message);
    }

    private void process(Job job) {

        String from = job.from();
        String instanceType = resourcesInfo.get(from).parameters().instanceType();
        Resource.ProviderType providerType = resourcesInfo.get(from).parameters().providerType();


        job.hostMetrics().metrics().stream().flatMap(m -> m.values().entrySet().stream())
                .forEach(e -> {

                    Double value = e.getValue();
                    if (value < 100) {
                        value = 1000. * value;
                    }
                    long rounded = Math.round(value);
                    String key = e.getKey();
                    updateMetric(providerType.name(), instanceType, from, key, rounded);
                });

        updateMetric(providerType.name(), instanceType, from, "duration", job.result().duration());

    }

    private void updateMetric(String providerType, String instanceType, String clientId, String metric, long value) {
        metricRegistry.histogram(providerType + ":" + metric).update(value);
        metricRegistry.histogram(providerType + ":" + instanceType + ":" + metric).update(value);
        metricRegistry.histogram(providerType + ":" + instanceType + ":" + clientId + ":" + metric).update(value);
    }
}
