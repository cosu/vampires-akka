package ro.cosu.vampires.client;

import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Option;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import ro.cosu.vampires.client.actors.ClientActor;
import ro.cosu.vampires.client.actors.MonitoringActor;
import ro.cosu.vampires.client.monitoring.MonitoringManager;
import ro.cosu.vampires.server.workload.ClientConfig;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Job;
import scala.concurrent.duration.Duration;

import java.util.Collections;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

public class ClientActorTest {

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
    public void testClientActor() throws Exception {

        TestActorRef.create(system, MonitoringActor
                .props(TestUtil.getMetricRegistryMock()), "monitor");

        TestActorRef<ClientActor> client = TestActorRef.create(system, ClientActor.props("test", "client1"), "client1");

        final JavaTestKit remoteProbe = new JavaTestKit(system);

        scala.Option<ActorRef> actorRefOption = Option.Some.option(remoteProbe.getRef()).asScala();
        client.tell(new ActorIdentity(null, actorRefOption), ActorRef.noSender());

        ClientInfo clientInfo = (ClientInfo) remoteProbe.receiveOne(Duration.create("500 milliseconds"));
        assertThat(clientInfo.executors().size(), not(0));
        ClientConfig clientConfig = ClientConfig.withDefaults().numberOfExecutors(1).build();
        client.tell(clientConfig, remoteProbe.getRef());
        Job job = (Job) remoteProbe.receiveOne(Duration.create("500 milliseconds"));
        assertThat(job.computation() ,is(Computation.empty()));

    }
}
