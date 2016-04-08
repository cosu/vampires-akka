package ro.cosu.vampires.server.actors;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import scala.concurrent.duration.Duration;

public class ResultActorTest extends AbstractActorTest {

    @Test
    public void testShutdown() throws Exception {

        final TestProbe testProbe = new TestProbe(system);
        final TestActorRef<ResultActor> sut = TestActorRef.create(system, ResultActor.props(1));
        testProbe.watch(sut);
        sut.tell(new ResourceControl.Shutdown(), ActorRef.noSender());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), Terminated.class);


    }
}
