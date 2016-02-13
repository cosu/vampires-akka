package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActor;
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
import ro.cosu.vampires.server.resources.mock.MockResourceParameters;
import scala.concurrent.duration.FiniteDuration;

public class ResourceActorTest extends AbstractActorTest {

    private ResourceProvider getLocalProvider() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new MockResourceModule());
            }
            @Provides
            @Named("Config")
            private Config provideConfig(){
                return ConfigFactory.load();
            }
        });
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        return rm.getProviders().get(Resource.Type.MOCK);
    }


    private ResourceControl.Create getCreateResource() {
        MockResourceParameters parameters = MockResourceParameters.builder().command("foo").build();
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

                resourceActor.tell(getCreateResource(), resourceProbe.getRef());


                resourceProbe.receiveN(2, FiniteDuration.create(1, "seconds"));


            }
        };

    }
}
