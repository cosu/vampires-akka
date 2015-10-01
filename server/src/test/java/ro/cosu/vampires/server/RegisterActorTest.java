package ro.cosu.vampires.server;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        TestActorRef<RegisterActor> registerActorRef = TestActorRef.create(system, RegisterActor.props(), "registerActor");
        TestActorRef<RegisterActor> newlyCreatedActor= TestActorRef.create(system, RegisterActor.props(), "newlyCreatedActor");


        registerActorRef.tell(new Message.Up(), newlyCreatedActor);

        RegisterActor registerActor = registerActorRef.underlyingActor();
        assertThat(registerActor.registered.size(), is(equalTo(1)));

        newlyCreatedActor.stop();

        assertThat(registerActor.registered.size(), is(equalTo(0)));


    }


}