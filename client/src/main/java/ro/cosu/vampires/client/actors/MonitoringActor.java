package ro.cosu.vampires.client.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ro.cosu.vampires.client.monitoring.MetricsWindow;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.JobStatus;
import ro.cosu.vampires.server.workload.Metric;
import ro.cosu.vampires.server.workload.Metrics;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.SortedMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class MonitoringActor extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private static final int MONITORING_INTERVAL_MILIS = 1000;

    private final MetricRegistry metricRegistry;

    private final MetricsWindow metricsWindow = new MetricsWindow();


    public static Props props(MetricRegistry registry) {

        return Props.create(MonitoringActor.class, registry);
    }

    MonitoringActor(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        recordMetrics();
    }


    @Override
    public void preStart() {
        getContext().system().scheduler().schedule(Duration.Zero(),
                Duration.create(MONITORING_INTERVAL_MILIS, MILLISECONDS), this::recordMetrics,
                getContext().system().dispatcher());
    }

    private void recordMetrics() {
        LocalDateTime now = LocalDateTime.now();
        SortedMap<String, Gauge> gauges = metricRegistry
                .getGauges((name, metric) -> name.startsWith("cpu") || name.startsWith("network"));
        metricsWindow.add(now, gauges);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.debug("{} from {}", message, getSender());

        if (message instanceof Job) {
            Job job = (Job) message;
            if (JobStatus.EXECUTED.equals(job.status())) {
                log.info("id: {} -> status:{} duration: {}", job.id(), job.status(), job.result().duration());
                job = getJobWithMetrics(job);
                getSender().tell(job, getSelf());
            } else {
                log.error("received workload result not present");
            }
        } else if (message instanceof Metrics) {
            // the client will ask for metrics to send to the server at boot time
            log.debug("got metrics request ");
            final ImmutableList<Metric> interval = metricsWindow.getInterval(
                    LocalDateTime.now().minus(MONITORING_INTERVAL_MILIS, ChronoUnit.MILLIS), LocalDateTime.now());
            final Metrics metrics = Metrics.builder().metadata(getHostMetrics()).metrics(interval).build();
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
        job = job.withHostMetrics(metrics);
        return job;
    }

    private ImmutableMap<String, String> getHostMetrics() {
        SortedMap<String, Gauge> hostGauges = metricRegistry.getGauges(
                (name, metric) -> name.startsWith("host"));

        return MetricsWindow.convertGaugesToString(hostGauges);
    }
}
