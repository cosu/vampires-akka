package ro.cosu.vampires.server.actors;

import com.google.common.collect.Maps;

import org.junit.Test;

import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import ro.cosu.vampires.server.workload.ClientConfig;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.Metrics;
import ro.cosu.vampires.server.workload.Result;
import scala.concurrent.duration.Duration;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DispatchActorTest extends AbstractActorTest {


    @Test
    public void testDispatchOfJob() {
        new JavaTestKit(system) {
            {
                // create a test probe
                final JavaTestKit workProbe = new JavaTestKit(system);

                // create a forwarder, injecting the probe's testActor
                final Props props = DispatchActor.props(workProbe.getRef());
                final ActorRef forwarder = system.actorOf(props, "dispatch");

                Job job = Job.builder()
                        .computation(Computation.builder().command("1").build())
                        .hostMetrics(Metrics.empty())
                        .result(Result.empty())
                        .build();

                // verify correct forwarding

                forwarder.tell(job, getRef());

                workProbe.expectMsgEquals(job);

                assertEquals(getRef(), workProbe.getLastSender());
            }
        };
    }

    @Test
    public void testDispatchOfConfig() {
        new JavaTestKit(system) {
            {
                final JavaTestKit workProbe = new JavaTestKit(system);

                final Props props = DispatchActor.props(workProbe.getRef());
                final ActorRef dispatchActor = system.actorOf(props, "dispatchActor");

                ClientInfo clientInfo = getClientInfo();

                dispatchActor.tell(clientInfo, workProbe.getRef());

                ClientConfig clientConfig = (ClientConfig) workProbe.receiveOne(Duration.create("1 second"));
                assertThat(clientConfig.cpuSetSize(), not(0));
            }
        };
    }

    private ClientInfo getClientInfo() {
        Map<String, Integer> executors = Maps.newHashMap();
        executors.put("FORK", 1);
        executors.put("DOCKER", 2);
        return ClientInfo.builder()
                .executors(executors)
                .metrics(Metrics.empty())
                .id("foo")
                .build();
    }

}
