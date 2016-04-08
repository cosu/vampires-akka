package ro.cosu.vampires.server.actors;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import akka.actor.ActorRef;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActor;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.resources.mock.MockResourceModule;
import ro.cosu.vampires.server.resources.mock.MockResourceParameters;
import scala.concurrent.duration.FiniteDuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ResourceActorTest extends AbstractActorTest {

    private ResourceProvider getLocalProvider() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new MockResourceModule());
            }

            @Provides
            @Named("Config")
            private Config provideConfig() {
                return ConfigFactory.load();
            }
        });
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        return rm.getProviders().get(Resource.Type.MOCK);
    }


    private ResourceControl.Create getCreateResource(String command) {
        MockResourceParameters parameters = MockResourceParameters.builder().command(command).build();
        return new ResourceControl.Create(Resource.Type.MOCK, parameters);
    }

    @Test
    public void testResourceActor() throws Exception {

        new JavaTestKit(system) {
            {
                final JavaTestKit resourceProbe = new JavaTestKit(system);
                ActorRef resourceActor = system.actorOf(ResourceActor.props(getLocalProvider()), "resourceActor");

                resourceProbe.setAutoPilot(new TestActor.AutoPilot() {
                    public TestActor.AutoPilot run(ActorRef sender, Object msg) {

                        if (msg instanceof ResourceInfo) {
                            ResourceInfo resourceInfo = (ResourceInfo) msg;
                            if (resourceInfo.status().equals(Resource.Status.RUNNING)) {
                                resourceActor.tell(new ResourceControl.Shutdown(), resourceProbe.getRef());
                            } else {
                                return noAutoPilot();
                            }
                        }
                        return this;
                    }
                });

                resourceActor.tell(getCreateResource("foo"), resourceProbe.getRef());

                resourceProbe.receiveN(2, FiniteDuration.create(1, "seconds"));

            }
        };
    }

    @Test
    public void testFailureOfResource() throws Exception {
        new JavaTestKit(system) {
            {
                final JavaTestKit resourceProbe = new JavaTestKit(system);
                ActorRef resourceActor = system.actorOf(ResourceActor.props(getLocalProvider()), "resourceActor1");

                TestActor.AutoPilot pilot = new TestActor.AutoPilot() {

                    public TestActor.AutoPilot run(ActorRef sender, Object msg) {

                        if (msg instanceof ResourceInfo) {
                            ResourceInfo resourceInfo = (ResourceInfo) msg;
                            if (resourceInfo.status().equals(Resource.Status.FAILED)) {
                                resourceActor.tell(new ResourceControl.Shutdown(), resourceProbe.getRef());
                                return noAutoPilot();
                            }
                        }
                        return this;
                    }
                };

                resourceProbe.setAutoPilot(pilot);

                resourceActor.tell(getCreateResource("fail"), resourceProbe.getRef());

                ResourceInfo ri = (ResourceInfo) resourceProbe.receiveOne(FiniteDuration.create(100, "seconds"));

                assertThat(ri.status(), is(Resource.Status.FAILED));
            }
        };

    }
}
