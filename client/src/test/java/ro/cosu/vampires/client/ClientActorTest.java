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
import ro.cosu.vampires.server.RegisterActor;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Job;

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
        ref.tell(Computation.builder().command("echo 1").id("test"), ActorRef.noSender());

        remoteProbe.expectMsgClass(Job.class);
        //TODO use a probe for this

//        remoteProbe.expectMsgClass(Message.Result.class);


    }
}
