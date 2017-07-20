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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import ro.cosu.vampires.server.actors.messages.configuration.CreateConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.DeleteConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.QueryConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.ResponseConfiguration;
import ro.cosu.vampires.server.actors.workload.ConfigurationsActor;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.resources.Configuration;
import ro.cosu.vampires.server.values.resources.ResourceDemand;
import ro.cosu.vampires.server.values.resources.ResourceDescription;
import scala.concurrent.duration.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class ConfigurationsActorTest extends AbstractActorTest {


    private CreateConfiguration getCreate() {
        Configuration localResource = Configuration.builder().description("localResource")
                .resources(ImmutableList.of(
                        ResourceDemand.builder().count(1)
                                .resourceDescription(
                                        ResourceDescription.builder()
                                                .provider(Resource.ProviderType.LOCAL).resourceType("local").cost(0).build()
                                )
                                .build()
                )).build();
        return CreateConfiguration.create(localResource, User.admin());
    }

    @Test
    public void create() throws Exception {
        TestProbe testProbe = new TestProbe(system);
        TestActorRef<ConfigurationsActor> configurationsActorTestActorRef =
                TestActorRef.create(system, ConfigurationsActor.props());

        configurationsActorTestActorRef.tell(getCreate(), testProbe.ref());

        // send a config with no price
        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ResponseConfiguration.class);
        ResponseConfiguration responseConfiguration = (ResponseConfiguration) testProbe.lastMessage().msg();

        assertThat(responseConfiguration.configurations().get(0).cost(), not(0.));

    }

    @Test
    public void get() throws Exception {
        TestProbe testProbe = new TestProbe(system);
        TestActorRef<ConfigurationsActor> configurationsActorTestActorRef =
                TestActorRef.create(system, ConfigurationsActor.props());

        configurationsActorTestActorRef.tell(getCreate(), testProbe.ref());
        configurationsActorTestActorRef.tell(getCreate(), testProbe.ref());
        configurationsActorTestActorRef.tell(getCreate(), testProbe.ref());

        configurationsActorTestActorRef.tell(QueryConfiguration.all(User.admin()), testProbe.ref());

        testProbe.receiveN(3);
        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ResponseConfiguration.class);
        ResponseConfiguration configurations = (ResponseConfiguration) testProbe.lastMessage().msg();

        assertThat(configurations.configurations().size(), is(3));

    }

    @Test
    public void delete() throws Exception {
        TestProbe testProbe = new TestProbe(system);
        TestActorRef<ConfigurationsActor> configurationsActorTestActorRef =
                TestActorRef.create(system, ConfigurationsActor.props());

        CreateConfiguration create = getCreate();
        configurationsActorTestActorRef.tell(create, testProbe.ref());

        testProbe.receiveN(1);

        DeleteConfiguration delete = DeleteConfiguration
                .create(Lists.newArrayList(create.configuration().id()), User.admin());
        configurationsActorTestActorRef.tell(delete, testProbe.ref());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ResponseConfiguration.class);
        ResponseConfiguration configurations = (ResponseConfiguration) testProbe.lastMessage().msg();
        assertThat(configurations.configurations().size(), is(1));

        configurationsActorTestActorRef.tell(QueryConfiguration.all(User.admin()), testProbe.ref());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ResponseConfiguration.class);
        configurations = (ResponseConfiguration) testProbe.lastMessage().msg();

        assertThat(configurations.configurations().size(), is(0));

    }
}