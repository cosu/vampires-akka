package ro.cosu.vampires.client;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.cosu.vampires.client.actors.MonitoringActor;
import ro.cosu.vampires.client.monitoring.MonitoringManager;
import ro.cosu.vampires.server.workload.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.LinkedList;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class MonitoringActorTest {

    private  static ActorSystem system;

    @BeforeClass
    public static void setUp() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void tearDown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }




    @Test
    public void testMetrics() throws Exception {

        TestActorRef<MonitoringActor> ref = TestActorRef.create(system, MonitoringActor
                .props(MonitoringManager.getMetricRegistry()));

        int seconds = 2;
        Thread.sleep((seconds + 1) * 1000);

        Computation computation = Computation.builder().command("test").id("test").build();
        Result result = Result.builder().duration(10).exitCode(0).output(new LinkedList<>())
                .trace(Trace.empty())
                .build();

        Job jobWithoutMetrics = Job.builder().computation(computation).result(result)
                .hostMetrics(Metrics.empty())
                .status(JobStatus.EXECUTED)
                .build();


        final Future<Object> future = akka.pattern.Patterns.ask(ref, jobWithoutMetrics, 3000);

        Job job = (Job) Await.result(future, Duration.create("2 seconds"));

        ImmutableList<Metric> timedMetrics = job.hostMetrics().metrics();

        assertThat(timedMetrics.size(), not(0));

        assertThat(job.hostMetrics().metadata().keySet().size(), not(0));


    }

    @Test
    public void testReplyToMetrics() throws Exception {
        TestActorRef<MonitoringActor> ref = TestActorRef.create(system, MonitoringActor
                .props(MonitoringManager.getMetricRegistry()));

        Thread.sleep(2000);

        final Future<Object> future = akka.pattern.Patterns.ask(ref, Metrics.empty(), 3000);

        Metrics metrics = (Metrics) Await.result(future, Duration.create("2 seconds"));

        ImmutableList<Metric> timedMetrics = metrics.metrics();

        assertThat(timedMetrics.size(), not(0));

        assertThat(metrics.metadata().keySet().size(), not(0));



    }
}
