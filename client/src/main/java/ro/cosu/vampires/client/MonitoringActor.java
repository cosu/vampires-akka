package ro.cosu.vampires.client;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import autovalue.shaded.com.google.common.common.collect.ImmutableMap;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import ro.cosu.vampires.client.monitoring.MetricsWindow;
import ro.cosu.vampires.server.Message;
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
                    SortedMap<String, Gauge> gauges = metricRegistry.getGauges();

                    metricsWindow.add(now, gauges);


                }, getContext().system().dispatcher());
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Message.Result) {
            Message.Result result = (Message.Result) message;


            ImmutableMap<LocalDateTime, ImmutableMap<String, Double>> metricsWindowInterval = metricsWindow.getInterval
                    (result.getResult().getStart(), result.getResult().getStop());
            result.getResult().setMetrics(metricsWindowInterval);

            getSender().tell(result, getSelf());

        }

    }
}
