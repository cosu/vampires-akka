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

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import ro.cosu.vampires.server.actors.messages.QueryResource;
import ro.cosu.vampires.server.actors.messages.ShutdownResource;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionInfo;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.Workload;

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
        new JavaTestKit(system) {
            {
                // create a test probe
                final JavaTestKit terminator = new JavaTestKit(system);
                final JavaTestKit restService = new JavaTestKit(system);

                Execution execution = getExecution();
                //
                final Props props = BootstrapActor.props(terminator.getRef());
                final ActorRef bootstrapActor = system.actorOf(props, "bootstrap");

                // start exec is fire and forget.
                bootstrapActor.tell(execution, restService.getRef());

                // query the state
                bootstrapActor.tell(QueryResource.withId(execution.id()), restService.getRef());
                Execution startedExecution = restService.expectMsgClass(Execution.class);
                assertThat(startedExecution.id(), is(execution.id()));
                assertThat(startedExecution.info().status(), is(ExecutionInfo.Status.STARTING));

                // shutdown
                bootstrapActor.tell(ShutdownResource.withId(execution.id()), restService.getRef());

                Execution stoppingExecution = restService.expectMsgClass(Execution.class);

                assertThat(stoppingExecution.id(), is(execution.id()));
                assertThat(stoppingExecution.info().status(), is(ExecutionInfo.Status.STOPPING));
                // let things stop cleanly
                Thread.sleep(100);
                // ask for the status
                bootstrapActor.tell(QueryResource.withId(execution.id()), restService.getRef());
                Execution canceledExecution = restService.expectMsgClass(Execution.class);
                // shoudl be cancled
                assertThat(canceledExecution.id(), is(execution.id()));
                assertThat(canceledExecution.info().status(), is(ExecutionInfo.Status.CANCELED));

            }
        };
    }
}