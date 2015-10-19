package ro.cosu.vampires.client;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.cosu.vampires.client.monitoring.MonitoringManager;
import ro.cosu.vampires.server.workload.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MonitoringActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }


    @Test
    public void testMetrics() throws Exception {
        int seconds = 2;

        TestActorRef<MonitoringActor> ref = TestActorRef.create(system, MonitoringActor
                .props(MonitoringManager.getMetricRegistry()), "monitoring");

        Thread.sleep(seconds*1000);

        Computation computation = Computation.builder().command("test").id("test").build();
        Result result = Result.builder().duration(10).exitCode(0).output(new LinkedList<>())
                .stop(LocalDateTime.now())
                .start(LocalDateTime.now().minus(seconds, ChronoUnit.SECONDS))
                .build();

        Workload workloadWithoutMetrics = Workload.builder().computation(computation).result(result)
                .metrics(Metrics.empty())
                .build();


        final Future<Object> future = akka.pattern.Patterns.ask(ref, workloadWithoutMetrics, 3000);

        Workload workload = (Workload) Await.result(future, Duration.create("2 seconds"));

        ImmutableList<Metric> timedMetrics = workload.metrics().metrics();

        assertThat(timedMetrics.size(), is(5));

        System.out.println(workload.metrics().metadata());

        assertThat(workload.metrics().metadata().keySet().size(), is(5));



    }


}
