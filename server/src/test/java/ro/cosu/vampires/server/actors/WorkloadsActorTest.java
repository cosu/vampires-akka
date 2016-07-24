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

import com.google.common.collect.Lists;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import ro.cosu.vampires.server.actors.messages.workload.CreateWorkload;
import ro.cosu.vampires.server.actors.messages.workload.DeleteWorkload;
import ro.cosu.vampires.server.actors.messages.workload.QueryWorkload;
import ro.cosu.vampires.server.actors.messages.workload.ResponseWorkload;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.jobs.WorkloadPayload;
import scala.concurrent.duration.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class WorkloadsActorTest extends AbstractActorTest {

    private CreateWorkload getCreate() {
        Config load = ConfigFactory.load("application-dev.conf");
        WorkloadPayload workloadPayload = WorkloadPayload.fromConfig(load.getConfig("vampires.workload"));
        Workload workload = Workload.fromPayload(workloadPayload);
        return CreateWorkload.create(workload, User.admin());
    }

    @Test
    public void create() throws Exception {
        TestProbe testProbe = new TestProbe(system);
        TestActorRef<WorkloadsActor> workloadsActorTestActorRef =
                TestActorRef.create(system, WorkloadsActor.props());

        workloadsActorTestActorRef.tell(getCreate(), testProbe.ref());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), Workload.class);
        Workload workload = (Workload) testProbe.lastMessage().msg();

        assertThat(workload.jobs().size(), not(0));

    }

    @Test
    public void get() throws Exception {
        TestProbe testProbe = new TestProbe(system);
        TestActorRef<WorkloadsActor> workloadsActorTestActorRef =
                TestActorRef.create(system, WorkloadsActor.props());

        workloadsActorTestActorRef.tell(getCreate(), testProbe.ref());
        workloadsActorTestActorRef.tell(getCreate(), testProbe.ref());
        workloadsActorTestActorRef.tell(getCreate(), testProbe.ref());

        workloadsActorTestActorRef.tell(QueryWorkload.all(User.admin()), testProbe.ref());

        testProbe.receiveN(3);
        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ResponseWorkload.class);
        ResponseWorkload configurations = (ResponseWorkload) testProbe.lastMessage().msg();

        assertThat(configurations.values().size(), is(3));

    }

    //
    @Test
    public void delete() throws Exception {
        TestProbe testProbe = new TestProbe(system);
        TestActorRef<WorkloadsActor> configurationsActorTestActorRef =
                TestActorRef.create(system, WorkloadsActor.props());

        CreateWorkload create = getCreate();
        configurationsActorTestActorRef.tell(create, testProbe.ref());

        testProbe.receiveN(1);

        DeleteWorkload delete = DeleteWorkload
                .create(Lists.newArrayList(create.workload().id()), User.admin());
        configurationsActorTestActorRef.tell(delete, testProbe.ref());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ResponseWorkload.class);
        ResponseWorkload configurations = (ResponseWorkload) testProbe.lastMessage().msg();
        assertThat(configurations.values().size(), is(1));

        configurationsActorTestActorRef.tell(QueryWorkload.all(User.admin()), testProbe.ref());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ResponseWorkload.class);
        configurations = (ResponseWorkload) testProbe.lastMessage().msg();

        assertThat(configurations.values().size(), is(0));

    }

}