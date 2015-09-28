package ro.cosu.vampires.client;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.cosu.vampires.server.Message;
import ro.cosu.vampires.server.Message.Computation;
import ro.cosu.vampires.server.RegisterActor;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class ExecutorActorTest {
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
    public void testExecutorActor() throws Exception {

        TestActorRef<RegisterActor> ref = TestActorRef.create(system, ExecutorActor.props(), "executor1");

        final Future<Object> future = akka.pattern.Patterns.ask(ref, new Computation("echo 1"), 3000);
        assertTrue(future.isCompleted());
        Message.Result result = (Message.Result) Await.result(future, Duration.Zero());

        assertThat(result.getResult().getExitCode(), is(0));

    }

}