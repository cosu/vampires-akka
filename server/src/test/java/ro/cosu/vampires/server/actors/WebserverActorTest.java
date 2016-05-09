package ro.cosu.vampires.server.actors;

import org.junit.Test;

import akka.testkit.TestActorRef;


public class WebserverActorTest extends AbstractActorTest {

    @Test
    public void create() throws Exception {

        TestActorRef<WebserverActor> webserverActor = TestActorRef.create(system, WebserverActor.props());

    }


}