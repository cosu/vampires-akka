package ro.cosu.vampires.server.actors;

import org.junit.Test;

import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class TerminatorTest extends AbstractActorTest {
    @Test
    public void testStart() throws Exception {
        final TestProbe testProbe = new TestProbe(system);

        TestActorRef<Terminator> testActorRef = TestActorRef.create(system, Terminator
                .props(), "terminator");

        testActorRef.tell(new ResourceControl.Up(), testProbe.ref());

        system.stop(testProbe.ref());


        Await.result(system.whenTerminated(), Duration.create("1 second"));

    }
}
