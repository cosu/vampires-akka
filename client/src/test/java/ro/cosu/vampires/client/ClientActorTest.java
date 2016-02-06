package ro.cosu.vampires.client;

import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Option;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.cosu.vampires.client.actors.ClientActor;
import ro.cosu.vampires.client.actors.MonitoringActor;
import ro.cosu.vampires.client.monitoring.MonitoringManager;
import ro.cosu.vampires.server.workload.ClientInfo;

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

        TestActorRef<MonitoringActor> monitor = TestActorRef.create(system, MonitoringActor
                .props(MonitoringManager.getMetricRegistry()), "monitor");

        TestActorRef<ClientActor> client = TestActorRef.create(system, ClientActor.props("test", "client1"), "client1");

        final JavaTestKit remoteProbe = new JavaTestKit(system);

        scala.Option<ActorRef> actorRefOption = Option.Some.option(remoteProbe.getRef()).asScala();
        client.tell(new ActorIdentity(null, actorRefOption), ActorRef.noSender());

        remoteProbe.expectMsgClass(ClientInfo.class);


    }
}
