package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.resources.mock.MockResourceModule;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ResourceManagerActorTest extends AbstractActorTest {

    private static final Resource.Type resourceType = Resource.Type.MOCK;

    @Test
    public void testStartResourceFail() throws Exception {
        final TestProbe testProbe = new TestProbe(system);

        ResourceControl.Create createResourceWhichFails = getCreateResource("fail");
        ResourceControl.Create createResourceWhichSucceeds = getCreateResource("foo");
        TestActorRef<ResourceManagerActor> resourceManagerActor = TestActorRef.create(system, ResourceManagerActor
                .props());

        resourceManagerActor.tell(createResourceWhichFails, ActorRef.noSender());
        resourceManagerActor.tell(createResourceWhichSucceeds, testProbe.ref());

        final Future<Object> infoFuture = Patterns.ask(resourceManagerActor, new ResourceControl.Info(),
                5000);

        ResourceInfo ci = (ResourceInfo) Await.result(infoFuture, Duration.create("5 seconds"));
        assertThat(ci.description().type(), is(equalTo(resourceType)));

    }

    @Test
    public void testStartResource() throws Exception {


        ResourceControl.Create createResource = getCreateResource("foo");


        TestActorRef<ResourceManagerActor> resourceManagerActor = TestActorRef.create(system, ResourceManagerActor
                .props());


        resourceManagerActor.tell(createResource, ActorRef.noSender());


        final Future<Object> infoFuture = Patterns.ask(resourceManagerActor, new ResourceControl.Info(),
                5000);

        ResourceInfo ci = (ResourceInfo) Await.result(infoFuture, Duration.create("5 seconds"));

        assertThat(ci.description().type(), is(equalTo(resourceType)));


        ResourceControl.Info resourceInfo = new ResourceControl.Info();

        final Future<Object> statusFuture = Patterns.ask(resourceManagerActor, resourceInfo, 5000);

        ResourceInfo si = (ResourceInfo) Await.result(statusFuture, Duration.create("5 seconds"));

        assertThat(si.status(), is(equalTo(Resource.Status.RUNNING)));

        final Future<Object> destroyFuture = Patterns.ask(resourceManagerActor, new ResourceControl
                .Shutdown(), 5000);

        ResourceInfo di = (ResourceInfo) Await.result(destroyFuture, Duration.create("5 seconds"));

        assertThat(di.status(), is(equalTo(Resource.Status.STOPPED)));

    }

    private AbstractModule getMockModule() {
        return new AbstractModule() {


            @Override
            protected void configure() {
                install(new MockResourceModule());
            }

            @Provides
            @Named("Config")
            Config provideConfig() {
                return ConfigFactory.empty();
            }

        };
    }


    private ResourceControl.Create getCreateResource(String command) {

        Injector injector = Guice.createInjector(getMockModule());
        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider resourceProvider = rm.getProviders().get(resourceType);

        Resource.Parameters parameters = resourceProvider.getBuilder().fromConfig(ConfigFactory.parseString
                ("command=" + command)).build();

        return new ResourceControl.Create(resourceType, parameters);
    }
}
