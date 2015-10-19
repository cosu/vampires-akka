package ro.cosu.vampires.client;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ro.cosu.vampires.client.monitoring.MetricsWindow;
import ro.cosu.vampires.server.workload.Metric;
import ro.cosu.vampires.server.workload.Metrics;
import ro.cosu.vampires.server.workload.Workload;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;
import java.util.SortedMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class MonitoringActor extends UntypedActor{
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    private final MetricRegistry metricRegistry;

    private final MetricsWindow metricsWindow = new MetricsWindow();


    public static Props props(MetricRegistry registry) {

        return Props.create(MonitoringActor.class, registry);
    }

    MonitoringActor(MetricRegistry metricRegistry){
        this.metricRegistry = metricRegistry;
    }


    @Override
    public void preStart(){
        getContext().system().scheduler().schedule(Duration.Zero(),
                Duration.create(500, MILLISECONDS), () -> {

                    LocalDateTime now = LocalDateTime.now();
                    SortedMap<String, Gauge> gauges = metricRegistry.getGauges((name, metric) -> name.startsWith("cpu") || name.startsWith("network"));

                    metricsWindow.add(now, gauges);


                }, getContext().system().dispatcher());
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Workload) {
            Workload result = (Workload) message;

            if (result.metrics().equals(Metrics.empty())){
                ImmutableList<Metric> metricsWindowInterval = metricsWindow.getInterval
                        (result.result().start(), result.result().stop());
                SortedMap<String, Gauge> hostGauges = metricRegistry.getGauges((name, metric) -> name.startsWith("host"));

                ImmutableMap<String, String> hostValues = MetricsWindow.convertGaugesToString(hostGauges);

                Metrics metrics = Metrics.builder().metadata(hostValues).metrics(metricsWindowInterval).build();
                result = result.toBuilder().metrics(metrics).build();
                getSender().tell(result, getSelf());
            }
            else
            {
                log.error("received workload result not present");
            }
        }
        else {
            log.error("received unknown type of message");
            unhandled(message);
        }

    }
}
