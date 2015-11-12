package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.testkit.TestActorRef;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.server.resources.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ResourceManagerActorTest extends AbstractActorTest {

    @Test
    public void testStartResource() throws Exception {


        ResourceControl.Create createResource = getCreateResource();


        TestActorRef<ResourceManagerActor> resourceManagerActor = TestActorRef.create(system, ResourceManagerActor
                .props(), "resourceManagerActor");


        resourceManagerActor.tell(createResource, ActorRef.noSender());


        final Future<Object> infoFuture = akka.pattern.Patterns.ask(resourceManagerActor, new ResourceControl.Info(),
                5000);

        ResourceInfo ci = (ResourceInfo) Await.result(infoFuture, Duration.create("5 seconds"));

        assertThat(ci.description().type(), is(equalTo(Resource.Type.SSH)));


        ResourceControl.Info resourceInfo = new ResourceControl.Info();

        final Future<Object> statusFuture = akka.pattern.Patterns.ask(resourceManagerActor, resourceInfo, 5000);

        ResourceInfo si = (ResourceInfo) Await.result(statusFuture, Duration.create("5 seconds"));

        assertThat(si.status(), is(equalTo(Resource.Status.RUNNING)));

        final Future<Object> destroyFuture = akka.pattern.Patterns.ask(resourceManagerActor, new ResourceControl
                .Shutdown(), 5000);

        ResourceInfo di = (ResourceInfo) Await.result(destroyFuture, Duration.create("5 seconds"));

        assertThat(di.status(), is(equalTo(Resource.Status.STOPPED)));

    }

    private ResourceControl.Create getCreateResource() {

        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.load().getConfig("vampires")));
        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider sshProvider = rm.getProviders().get(Resource.Type.SSH);
        Resource.Parameters parameters = sshProvider.getParameters("local");

        return new ResourceControl.Create(Resource.Type.SSH, parameters);
    }
}
