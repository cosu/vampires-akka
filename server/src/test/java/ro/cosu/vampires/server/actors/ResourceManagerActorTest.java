/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.actors;

import akka.pattern.Patterns;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.resources.mock.MockResourceModule;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Metrics;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ResourceManagerActorTest extends AbstractActorTest {

    private static final Resource.Type RESOURCE_TYPE = Resource.Type.MOCK;
    private static ResourceManager rm;

    @BeforeClass
    public static void setUp() {
        AbstractModule mockModule = getMockModule();
        Injector injector = Guice.createInjector(mockModule);
        rm = injector.getInstance(ResourceManager.class);
    }

    private static AbstractModule getMockModule() {
        return new AbstractModule() {

            @Override
            protected void configure() {
                install(new MockResourceModule());
            }

            @Provides
            @Named("Config")
            Config provideConfig() {
                return ConfigFactory.parseString("mock.foo={}");
            }

        };
    }

    @Test
    public void testStartResourceFail() throws Exception {
        final TestProbe testProbe = new TestProbe(system);

        ResourceControl.Create createResourceWhichFails = getCreateResource("fail");
        TestActorRef<ResourceManagerActor> resourceManagerActor = TestActorRef.create(system,
                ResourceManagerActor.props());

        resourceManagerActor.tell(createResourceWhichFails, testProbe.ref());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ResourceInfo.class);

        final ResourceInfo resourceInfo = (ResourceInfo) testProbe.lastMessage().msg();

        assertThat(resourceInfo.status(), is(Resource.Status.FAILED));

    }

    @Test
    public void testStartResource() throws Exception {
        final TestProbe testProbe = new TestProbe(system);

        ResourceControl.Create createResourceWhichFails = getCreateResource("foo");
        TestActorRef<ResourceManagerActor> resourceManagerActor = TestActorRef.create(system,
                ResourceManagerActor.props());

        resourceManagerActor.tell(createResourceWhichFails, testProbe.ref());

        assertThat(resourceManagerActor.underlyingActor().resourceRegistry.getResourceActors().size(), is(1));

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ResourceInfo.class);

        final ResourceInfo resourceInfo = (ResourceInfo) testProbe.lastMessage().msg();

        assertThat(resourceInfo.status(), is(Resource.Status.RUNNING));

    }

    @Test
    public void testStartStopResource() throws Exception {

        final TestProbe testProbe = new TestProbe(system);

        ResourceControl.Create createResource = getCreateResource("foo");

        TestActorRef<ResourceManagerActor> resourceManagerActor = TestActorRef.create(system, ResourceManagerActor
                .props());

        resourceManagerActor.tell(createResource, testProbe.ref());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ResourceInfo.class);

        ResourceInfo resourceInfo = (ResourceInfo) testProbe.lastMessage().msg();

        assertThat(resourceInfo.status(), is(equalTo(Resource.Status.RUNNING)));

        Thread.sleep(100);

        String id = resourceInfo.description().id();

        ClientInfo clientInfo = ClientInfo.builder()
                .executors(Maps.newHashMap())
                .metrics(Metrics.empty())
                .id(id)
                .build();

        resourceManagerActor.tell(clientInfo, testProbe.ref());

        ResourceControl.Query resourceQuery = new ResourceControl.Query(id);

        final Future<Object> infoFuture = Patterns.ask(resourceManagerActor, resourceQuery, 1000);

        resourceInfo = (ResourceInfo) Await.result(infoFuture, Duration.create("1 seconds"));

        assertThat(resourceInfo.status(), is(Resource.Status.CONNECTED));

        Future<Object> destroyFuture = Patterns.ask(resourceManagerActor, new ResourceControl.Shutdown(), 1000);

        ResourceInfo di = (ResourceInfo) Await.result(destroyFuture, Duration.create("1 seconds"));

        assertThat(di.status(), is(equalTo(Resource.Status.STOPPED)));

    }

    @Test
    public void testBootstrap() throws Exception {
        final TestProbe testProbe = new TestProbe(system);

        TestActorRef<ResourceManagerActor> resourceManagerActor = TestActorRef.create(system,
                ResourceManagerActor.props());
        ResourceControl.Bootstrap bs = new ResourceControl.Bootstrap(RESOURCE_TYPE, "foo");
        resourceManagerActor.tell(bs, testProbe.ref());
        ResourceInfo ri = (ResourceInfo) testProbe.receiveOne(Duration.create("50 milliseconds"));
        assertThat(ri.status(), equalTo(Resource.Status.RUNNING));
        assertThat(ri.description().provider(), equalTo(Resource.Type.MOCK));

    }

    private ResourceControl.Create getCreateResource(String command) {

        ResourceProvider resourceProvider = rm.getProviders().get(RESOURCE_TYPE);

        Resource.Parameters parameters = resourceProvider.getBuilder().fromConfig(ConfigFactory.parseString
                ("command=" + command)).build();

        return new ResourceControl.Create(RESOURCE_TYPE, parameters);
    }
}
