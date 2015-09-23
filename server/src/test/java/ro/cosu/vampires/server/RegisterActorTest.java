package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class RegisterActorTest {

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
        TestActorRef<RegisterActor> ref = TestActorRef.create(system, RegisterActor.props(), "register1");

        ref.tell(new Message.Up(), ActorRef.noSender());

        RegisterActor registerActor = ref.underlyingActor();
        assertThat(registerActor.registered.size(), is(equalTo(1)));
    }


}