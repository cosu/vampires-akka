package ro.cosu.vampires.server.actors;

import org.junit.Test;

import java.util.HashMap;

import akka.actor.ActorRef;
import akka.testkit.TestProbe;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceDescription;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Metrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class ResourceRegistryTest extends AbstractActorTest {

    private static ActorRef createActorRef() {
        TestProbe testProbe = new TestProbe(system);
        return testProbe.ref();
    }

    @Test
    public void testAddResource() throws Exception {
        final ResourceRegistry resourceRegistry = new ResourceRegistry();
        resourceRegistry.addResourceActor(createActorRef());
        assertThat(resourceRegistry.getResourceActors().size(), is(1));

    }

    @Test
    public void testRegisterClient() throws Exception {
        final ResourceRegistry resourceRegistry = new ResourceRegistry();
        final ActorRef actorRef = createActorRef();
        resourceRegistry.addResourceActor(actorRef);

        resourceRegistry.registerClient(actorRef, ClientInfo.builder().executors(new HashMap<>()).id
                ("foo").metrics(Metrics.empty()).build());
        assertThat(resourceRegistry.getRegisteredClients().size(), is(1));

    }

    @Test
    public void testLookupResource() throws Exception {

        final ResourceRegistry resourceRegistry = new ResourceRegistry();
        final ActorRef actorRef = createActorRef();
        resourceRegistry.addResourceActor(actorRef);

        final ResourceInfo resourceInfo = ResourceInfo.create(ResourceDescription.create("foo", Resource.Type.MOCK),
                Resource.Status.RUNNING);
        resourceRegistry.registerResource(actorRef, resourceInfo);
        final ClientInfo clientInfo = ClientInfo.builder().executors(new HashMap<>()).id
                ("foo").metrics(Metrics.empty()).build();
        resourceRegistry.registerClient(actorRef, clientInfo);

        final ActorRef foo = resourceRegistry.lookupResourceOfClient(clientInfo.id()).get();
        assertThat(foo, notNullValue());
    }


    @Test
    public void testRegisterResource() throws Exception {
        final ResourceRegistry resourceRegistry = new ResourceRegistry();
        final ActorRef actorRef = createActorRef();
        resourceRegistry.addResourceActor(actorRef);

        final ResourceInfo resourceInfo = ResourceInfo.create(ResourceDescription.create("foo", Resource.Type.MOCK),
                Resource.Status.RUNNING);
        resourceRegistry.registerResource(actorRef, resourceInfo);
        final ActorRef foo = resourceRegistry.lookupResourceOfClient("foo").get();

        assertThat(foo, notNullValue());

    }

    @Test
    public void testRemoveResource() throws Exception {
        final ResourceRegistry resourceRegistry = new ResourceRegistry();
        final ActorRef actorRef = createActorRef();
        resourceRegistry.addResourceActor(actorRef);
        resourceRegistry.removeResource(actorRef);
        assertThat(resourceRegistry.getResourceActors().size(), is(0));
    }
}
