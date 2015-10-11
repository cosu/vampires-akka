package ro.cosu.vampires.server;

import akka.testkit.TestActorRef;
import org.junit.Test;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ResourceProviderActorTest extends AbstractActorTest{

    @Test
    public void testStartResource() throws Exception {

        Message.CreateResource createResource = new Message.CreateResource(Resource.Type.LOCAL);


        TestActorRef<RegisterActor> resourceManagerActor = TestActorRef.create(system, ResourceManagerActor.props(),
                "registerActor");


        final Future<Object> createFuture = akka.pattern.Patterns.ask(resourceManagerActor,createResource , 3000);

        ResourceInfo ri = (ResourceInfo) Await.result(createFuture, Duration.create("5 seconds"));

        assertThat(ri.description().type(), is(equalTo(Resource.Type.LOCAL)));

        final Future<Object> destroyFuture = akka.pattern.Patterns.ask(resourceManagerActor,new Message.DestroyResource(ri.description()), 3000);

        ResourceInfo di = (ResourceInfo) Await.result(destroyFuture, Duration.create("5 seconds"));

        assertThat(di.status(), is(equalTo(Resource.Status.FAILED)));


    }
}
