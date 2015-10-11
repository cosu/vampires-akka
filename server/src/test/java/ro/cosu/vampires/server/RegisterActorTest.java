package ro.cosu.vampires.server;

import akka.testkit.TestActorRef;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class RegisterActorTest extends AbstractActorTest {

    @Test
    public void testRegisterActorRegistration() throws InterruptedException {

        TestActorRef<RegisterActor> registerActor = TestActorRef.create(system, RegisterActor.props(), "registerActor");

        Thread.sleep(1000);
        assertThat(registerActor.underlyingActor().registered.size(), not(0));


    }


}
