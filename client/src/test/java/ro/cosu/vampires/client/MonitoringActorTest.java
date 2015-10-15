package ro.cosu.vampires.client;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import autovalue.shaded.com.google.common.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.cosu.vampires.client.monitoring.MonitoringManager;
import ro.cosu.vampires.server.ExecResult;
import ro.cosu.vampires.server.Message;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
    public void testName() throws Exception {
        int seconds = 2;



        TestActorRef<MonitoringActor> ref = TestActorRef.create(system, MonitoringActor
                .props(MonitoringManager.getMetricRegistry()), "monitoring");

        Thread.sleep(seconds*1000);

        ExecResult execResult= new ExecResult.Builder()
                .start(LocalDateTime.now().minus(seconds, ChronoUnit.SECONDS))
                .stop(LocalDateTime.now()).build();

        Message.Result result = new Message.Result(execResult,  new Message.Computation("test"));

        final Future<Object> future = akka.pattern.Patterns.ask(ref, result, 3000);

        Message.Result resultWithMetrics = (Message.Result) Await.result(future, Duration.create("2 seconds"));

        ImmutableMap<LocalDateTime, ImmutableMap<String, Double>> metrics = resultWithMetrics.getResult().getMetrics();

        assertThat(metrics.keySet().size(), is(4));


    }


}
