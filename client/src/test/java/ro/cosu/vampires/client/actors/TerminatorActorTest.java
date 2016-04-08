package ro.cosu.vampires.client.actors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created on 14-2-16.
 */
public class TerminatorActorTest {
    private static ActorSystem system;

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
    public void testTerminatorActor() throws Exception {
        TestProbe testProbe = new TestProbe(system);

        TestActorRef.create(system, TerminatorActor.props(testProbe.ref()));
        testProbe.ref().tell(PoisonPill.getInstance(), TestActorRef.noSender());

        Await.result(system.whenTerminated(), Duration.create("1 second"));
        assertThat(system.whenTerminated().value().get().isSuccess(), is(true));

    }

}