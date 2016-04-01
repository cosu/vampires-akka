package ro.cosu.vampires.client;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import ro.cosu.vampires.client.actors.MonitoringActor;
import ro.cosu.vampires.client.monitoring.MonitoringManager;
import ro.cosu.vampires.server.workload.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeMap;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

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

        Result result = Result.builder().duration(10).exitCode(0).output(new LinkedList<>())
                .trace(Trace.builder()
                        .start(now.minusSeconds(1))
                        .stop(now)
                        .cpuSet(Sets.newHashSet(1)).executor("foo").totalCpuCount(1)
                        .executorMetrics(Metrics.empty())
                    .build()
                )
                .build();

        Job jobWithoutMetrics = Job.builder().computation(computation).result(result)
                .hostMetrics(Metrics.empty())
                .status(JobStatus.EXECUTED)
                .build();

        final Future<Object> future = akka.pattern.Patterns.ask(ref, jobWithoutMetrics, 50);

        Job job = (Job) Await.result(future, Duration.create("50 milliseconds"));

        ImmutableList<Metric> timedMetrics = job.hostMetrics().metrics();

        assertThat(job.hostMetrics().metadata().keySet().size(), not(0));
        assertThat(timedMetrics.size(), not(0));
    }

    @Test
    public void testReplyToMetrics() throws Exception {
        TestActorRef<MonitoringActor> ref = TestActorRef.create(system, MonitoringActor
                .props(TestUtil.getMetricRegistryMock()));

        final Future<Object> future = akka.pattern.Patterns.ask(ref, Metrics.empty(), 50);

        Metrics metrics = (Metrics) Await.result(future, Duration.create("50 milliseconds"));
        assertThat(metrics.metadata().keySet().size(), not(0));

        ImmutableList<Metric> timedMetrics = metrics.metrics();
        assertThat(timedMetrics.size(), not(0));

        System.out.println(metrics);

    }
}
