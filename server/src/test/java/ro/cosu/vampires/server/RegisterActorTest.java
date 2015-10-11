package ro.cosu.vampires.server;

import akka.testkit.TestActorRef;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RegisterActorTest extends AbstractActorTest {

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
