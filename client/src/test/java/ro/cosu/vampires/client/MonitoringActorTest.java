package ro.cosu.vampires.client;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import autovalue.shaded.com.google.common.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.cosu.vampires.client.actors.MonitoringActor;
import ro.cosu.vampires.server.workload.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;

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
                .props(TestUtil.getMetricRegistryMock()));

        Computation computation = Computation.builder().command("test").id("test").build();
        LocalDateTime now = LocalDateTime.now();

        Result result = Result.builder()
                .duration(1)
                .exitCode(0)
                .output(Lists.newLinkedList())
                .trace(Trace.builder()
                        .start(now.minusSeconds(1))
                        .stop(now.plusSeconds(1))
                        .cpuSet(Sets.newHashSet(1)).executor("foo").totalCpuCount(1)
                        .executorMetrics(Metrics.empty())
                    .build())
                .build();

        Job jobWithoutMetrics = Job.builder().computation(computation).result(result)
                .hostMetrics(Metrics.empty())
                .status(JobStatus.EXECUTED)
                .build();

        Future<Object> future = akka.pattern.Patterns.ask(ref, jobWithoutMetrics, 1000);

        Job job = (Job) Await.result(future, Duration.create("1 seconds"));

        assertThat(job.hostMetrics().metadata().keySet().size(), not(0));
    }

    @Test
    public void testReplyToMetrics() throws Exception {
        TestActorRef<MonitoringActor> ref = TestActorRef.create(system, MonitoringActor
                .props(TestUtil.getMetricRegistryMock()));

        Future<Object> future = akka.pattern.Patterns.ask(ref, Metrics.empty(), 1000);

        Metrics metrics = (Metrics) Await.result(future, Duration.create("1 seconds"));

        assertThat(metrics.metadata().keySet().size(), not(0));

    }
}
