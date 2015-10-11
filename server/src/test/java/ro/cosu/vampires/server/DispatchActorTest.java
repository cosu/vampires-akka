package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DispatchActorTest extends AbstractActorTest{


    @Test
    public void testDispatch(){
        new JavaTestKit(system) {
            {
                // create a test probe
                final JavaTestKit workProbe = new JavaTestKit(system);
                final JavaTestKit resultProbe = new JavaTestKit(system);
                final JavaTestKit registerProbe = new JavaTestKit(system);


                // create a forwarder, injecting the probe’s testActor
                final Props props = DispatchActor.props(workProbe.getRef(),resultProbe.getRef(), registerProbe.getRef());
                final ActorRef forwarder = system.actorOf(props, "dispatch");

                Message.Up up = new Message.Up();
                Message.Request request = new Message.Request();
                Message.Result result = new Message.Result(null, null);

                // verify correct forwarding
                forwarder.tell(up, getRef());
                forwarder.tell(request, getRef());
                forwarder.tell(result, getRef());

                workProbe.expectMsgEquals(request);
                resultProbe.expectMsgEquals(result);

                assertEquals(getRef(), workProbe.getLastSender());
            }
        };
    }

}
