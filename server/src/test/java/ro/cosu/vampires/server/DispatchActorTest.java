package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DispatchActorTest {
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
    public void testDispatch(){
        new JavaTestKit(system) {
            {
                // create a test probe
                final JavaTestKit workProbe = new JavaTestKit(system);
                final JavaTestKit resultProbe = new JavaTestKit(system);


                // create a forwarder, injecting the probe’s testActor
                final Props props = DispatchActor.props(workProbe.getRef(),resultProbe.getRef());
                final ActorRef forwarder = system.actorOf(props, "dispatch");

                Message.Request request = new Message.Request();
                Message.Result result = new Message.Result(null, null);

                // verify correct forwarding
                forwarder.tell(request, getRef());
                forwarder.tell(result, getRef());

                workProbe.expectMsgEquals(request);
                resultProbe.expectMsgEquals(result);

                assertEquals(getRef(), workProbe.getLastSender());
            }
        };
    }

}