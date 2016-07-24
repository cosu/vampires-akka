/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.actors;

import org.junit.Test;

import java.util.HashMap;

import akka.actor.ActorRef;
import akka.testkit.TestProbe;
import ro.cosu.vampires.server.actors.resource.ResourceRegistry;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.mock.MockResourceParameters;
import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class ResourceRegistryTest extends AbstractActorTest {


    private static ActorRef createActorRef() {
        TestProbe testProbe = new TestProbe(system);
        return testProbe.ref();
    }

    private Resource.Parameters getParameters() {
        return MockResourceParameters.builder()
                .instanceType("foo")
                .id("foo")
                .command("foo")
                .build();
    }

    @Test
    public void testAddResource() throws Exception {
        final ResourceRegistry resourceRegistry = new ResourceRegistry();
        resourceRegistry.addResourceActor(createActorRef(), getParameters());
        assertThat(resourceRegistry.getResourceActors().size(), is(1));

    }

    @Test
    public void testRegisterClient() throws Exception {
        final ResourceRegistry resourceRegistry = new ResourceRegistry();
        final ActorRef actorRef = createActorRef();
        resourceRegistry.addResourceActor(actorRef, getParameters());

        resourceRegistry.registerClient(actorRef, ClientInfo.builder().executors(new HashMap<>()).id
                ("foo").metrics(Metrics.empty()).build());
        assertThat(resourceRegistry.getRegisteredClients().size(), is(1));

    }

    @Test
    public void testLookupResource() throws Exception {

        final ResourceRegistry resourceRegistry = new ResourceRegistry();
        final ActorRef actorRef = createActorRef();
        resourceRegistry.addResourceActor(actorRef, getParameters());

        final ResourceInfo resourceInfo = ResourceInfo.create(getParameters(),
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
        resourceRegistry.addResourceActor(actorRef, getParameters());

        final ResourceInfo resourceInfo = ResourceInfo.create(getParameters(),
                Resource.Status.RUNNING);
        resourceRegistry.registerResource(actorRef, resourceInfo);
        final ActorRef foo = resourceRegistry.lookupResourceOfClient("foo").get();

        assertThat(foo, notNullValue());

    }

    @Test
    public void testRemoveResource() throws Exception {
        final ResourceRegistry resourceRegistry = new ResourceRegistry();
        final ActorRef actorRef = createActorRef();
        resourceRegistry.addResourceActor(actorRef, getParameters());
        resourceRegistry.removeResource(actorRef);
        assertThat(resourceRegistry.getResourceActors().size(), is(0));
    }
}
