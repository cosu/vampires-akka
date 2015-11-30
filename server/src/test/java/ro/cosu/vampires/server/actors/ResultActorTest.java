package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class ResultActorTest extends AbstractActorTest{

    @Test
    public void testShutdown() throws Exception {

        final TestProbe testProbe = new TestProbe(system);
        final TestActorRef<ResultActor> sut = TestActorRef.create(system, ResultActor.props(1));
        testProbe.watch(sut);
        sut.tell(new ResourceControl.Shutdown(), ActorRef.noSender());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), Terminated.class);


    }
}
