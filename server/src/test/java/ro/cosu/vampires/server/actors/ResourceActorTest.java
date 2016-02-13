package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActor;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.server.resources.*;
import ro.cosu.vampires.server.resources.local.LocalResourceParameters;
import scala.concurrent.duration.FiniteDuration;

public class ResourceActorTest extends AbstractActorTest {

    private ResourceProvider getLocalProvider() {
        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.load()));
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        return rm.getProviders().get(Resource.Type.LOCAL);
    }


    private ResourceControl.Create getCreateResource() {
        LocalResourceParameters parameters = LocalResourceParameters.builder().command("sleep 1").build();
        return new ResourceControl.Create(Resource.Type.LOCAL, parameters);
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

                resourceProbe.receiveN(2, FiniteDuration.create(3, "seconds"));


            }
        };

    }
}
