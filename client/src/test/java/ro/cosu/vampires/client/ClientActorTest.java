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
import ro.cosu.vampires.server.Message;
import ro.cosu.vampires.server.RegisterActor;

public class ClientActorTest {

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
    public void testRegisterActorRegistration() {
        TestActorRef<RegisterActor> ref = TestActorRef.create(system, ClientActor.props("test"), "client1");
        final JavaTestKit remoteProbe = new JavaTestKit(system);


        scala.Option<ActorRef> actorRefOption = Option.Some.option(remoteProbe.getRef()).asScala();
        ref.tell(new ActorIdentity(null,actorRefOption), ActorRef.noSender());
        ref.tell(new Message.Computation("echo 1"), ActorRef.noSender());

        remoteProbe.expectMsgClass(Message.Up.class);
        remoteProbe.expectMsgClass(Message.Request.class);
        //TODO use a probe for this

//        remoteProbe.expectMsgClass(Message.Result.class);


    }
}
