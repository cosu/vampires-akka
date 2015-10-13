package ro.cosu.vampires.server;

import akka.testkit.TestActorRef;
import org.junit.Test;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.local.LocalResourceParameters;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ResourceManagerActorTest extends AbstractActorTest{

    @Test
    public void testStartResource() throws Exception {

        Message.CreateResource createResource = getCreateResource();


        TestActorRef<ResourceManagerActor> resourceManagerActor = TestActorRef.create(system, ResourceManagerActor.props(),
                "resourceManagerActor");


        final Future<Object> createFuture = akka.pattern.Patterns.ask(resourceManagerActor,createResource , 3000);

        ResourceInfo ci = (ResourceInfo) Await.result(createFuture, Duration.create("5 seconds"));

        assertThat(ci.description().type(), is(equalTo(Resource.Type.LOCAL)));


        Message.GetResourceInfo resourceInfo = new Message.GetResourceInfo(ci.description());

        final Future<Object> statusFuture = akka.pattern.Patterns.ask(resourceManagerActor, resourceInfo , 3000);

        ResourceInfo si = (ResourceInfo) Await.result(statusFuture, Duration.create("5 seconds"));

        assertThat(si.status(), is(equalTo(Resource.Status.RUNNING)));

        final Future<Object> destroyFuture = akka.pattern.Patterns.ask(resourceManagerActor,new Message.DestroyResource(ci.description()), 3000);

        ResourceInfo di = (ResourceInfo) Await.result(destroyFuture, Duration.create("5 seconds"));

        assertThat(di.status(), is(equalTo(Resource.Status.STOPPED)));


    }

    private Message.CreateResource getCreateResource() {
        LocalResourceParameters parameters = LocalResourceParameters.builder().command("sleep 10").build();

        return new Message.CreateResource(Resource.Type.LOCAL, parameters);
    }
}
