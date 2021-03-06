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

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.testkit.TestKit;
import ro.cosu.vampires.server.actors.messages.execution.DeleteExecution;
import ro.cosu.vampires.server.actors.messages.execution.QueryExecution;
import ro.cosu.vampires.server.actors.messages.execution.ResponseExecution;
import ro.cosu.vampires.server.actors.messages.execution.StartExecution;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.ExecutionInfo;
import ro.cosu.vampires.server.values.jobs.ExecutionMode;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.resources.Configuration;
import scala.concurrent.duration.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BootstrapActorTest extends AbstractActorTest {
    private Execution getExecution() {
        Configuration configuration = Configuration.builder().resources(ImmutableList.of()).description("foo").build();
        Workload workload = Workload.builder().format("foo").sequenceStart(0).sequenceStop(1).task("bar").build();

        return Execution.builder().configuration(configuration)
                .type(ExecutionMode.FULL).info(ExecutionInfo.empty()).workload(workload).build();
    }

    @Test
    public void start() throws Exception {
        new TestKit(system) {
            {
                // create a test probe
                final TestKit terminator = new TestKit(system);
                final TestKit restService = new TestKit(system);
                final ActorRef bootstrapActor = system.actorOf(BootstrapActor.props(terminator.testActor()), "bootstrap");

                terminator.expectMsgClass(Duration.create(10, TimeUnit.SECONDS), ResourceControl.Up.class);

                // start exec is fire and forget.
                Execution execution = getExecution();
                StartExecution startExecution = StartExecution.create(User.admin(), execution);
                bootstrapActor.tell(startExecution, restService.testActor());

                // query the state
                bootstrapActor.tell(QueryExecution.create(execution.id(), User.admin()), restService.testActor());
                ResponseExecution responseExecution = restService.expectMsgClass(ResponseExecution.class);
                Execution startedExecution = responseExecution.values().get(0);
                assertThat(startedExecution.id(), is(execution.id()));
                assertThat(startedExecution.info().status(), is(ExecutionInfo.Status.STARTING));

                // shutdown
                bootstrapActor.tell(DeleteExecution.create(execution.id(), User.admin()), restService.testActor());

                responseExecution = restService.expectMsgClass(ResponseExecution.class);

                Execution stoppingExecution = responseExecution.values().get(0);

                assertThat(stoppingExecution.id(), is(execution.id()));
                assertThat(stoppingExecution.info().status(), is(ExecutionInfo.Status.STOPPING));
                // let things stop cleanly

                // ask for the status
                bootstrapActor.tell(QueryExecution.create(execution.id(), User.admin()), restService.testActor());
                responseExecution = restService.expectMsgClass(ResponseExecution.class);
                Execution canceledExecution = responseExecution.values().get(0);

                // retry a bit (actors comms is async)
                int count = 0;
                while (count < 3 && !canceledExecution.info().status().equals(ExecutionInfo.Status.CANCELED)) {
                    Thread.sleep(2000);
                    count++;
                    bootstrapActor.tell(QueryExecution.create(execution.id(), User.admin()), restService.testActor());
                    responseExecution = restService.expectMsgClass(ResponseExecution.class);
                    canceledExecution = responseExecution.values().get(0);
                }
                assertThat(canceledExecution.id(), is(execution.id()));
                assertThat(canceledExecution.info().status(), is(ExecutionInfo.Status.CANCELED));

            }
        };
    }
}