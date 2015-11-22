package ro.cosu.vampires.client;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ForkExecutorActorTest {
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


        //TODO use a probe for this
//        TestActorRef<RegisterActor> ref = TestActorRef.create(system, ExecutorActor.props(), "executor1");
//
//        final Future<Object> future = akka.pattern.Patterns.ask(ref, new Computation("echo 1"), 3000);
//        assertTrue(future.isCompleted());
//        Message.Result result = (Message.Result) Await.result(future, Duration.Zero());
//
//        assertThat(result.getResult().getExitCode(), is(0));

    }

}
