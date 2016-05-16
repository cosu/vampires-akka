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
import com.google.common.collect.Maps;

import org.junit.Test;

import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.testkit.JavaTestKit;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.workload.ClientConfig;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionInfo;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.Metrics;
import ro.cosu.vampires.server.workload.Result;
import ro.cosu.vampires.server.workload.Workload;
import scala.concurrent.duration.Duration;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class ExecutionActorTest extends AbstractActorTest {

    private Execution getExec() {
        Configuration configuration = Configuration.builder().resources(ImmutableList.of()).description("foo").build();
        Workload workload = Workload.builder()
                .format("%d")
                .url("")
                .sequenceStart(0).sequenceStop(10).task("bar").build();

        return Execution.builder().configuration(configuration).type(ExecutionMode.FULL)
                .info(ExecutionInfo.empty())
                .workload(workload).build();
    }

    @Test
    public void testDispatchOfJob() {

        new JavaTestKit(system) {
            {
                // create a test probe
                final JavaTestKit workProbe = new JavaTestKit(system);

                final ActorRef executionActor = system.actorOf(ExecutionActor.props(getExec()));

                // this is a job request
                Job job = Job.builder()
                        .computation(Computation.builder().command("1").build())
                        .hostMetrics(Metrics.empty())
                        .result(Result.empty())
                        .build();
                // send it
                executionActor.tell(job, workProbe.getRef());

                // expect a full job back
                workProbe.expectMsgClass(Job.class);
            }
        };
    }

    @Test
    public void testDispatchOfConfig() {
        new JavaTestKit(system) {
            {
                final JavaTestKit workProbe = new JavaTestKit(system);

                final ActorRef executionActor = system.actorOf(ExecutionActor.props(getExec()));

                ClientInfo clientInfo = getClientInfo();

                executionActor.tell(clientInfo, workProbe.getRef());

                ClientConfig clientConfig = (ClientConfig) workProbe.receiveOne(Duration.create("1 second"));
                assertThat(clientConfig.cpuSetSize(), not(0));
            }
        };
    }

    private ClientInfo getClientInfo() {
        Map<String, Integer> executors = Maps.newHashMap();
        executors.put("FORK", 1);
        executors.put("DOCKER", 2);
        return ClientInfo.builder()
                .executors(executors)
                .metrics(Metrics.empty())
                .id("foo")
                .build();
    }


    @Test
    public void testShutdown() {

        new JavaTestKit(system) {
            {
                Execution exec = getExec();
                // create a test probe
                final JavaTestKit workProbe = new JavaTestKit(system);

                final ActorRef executionActor = system.actorOf(ExecutionActor.props(exec));

                workProbe.watch(executionActor);

                executionActor.tell(ResourceControl.Shutdown.create(), workProbe.getRef());

                workProbe.expectMsgClass(Terminated.class);

            }
        };
    }
}
