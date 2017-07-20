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
import akka.testkit.TestKit;
import ro.cosu.vampires.server.actors.execution.ExecutionActor;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.values.ClientConfig;
import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Computation;
import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.ExecutionInfo;
import ro.cosu.vampires.server.values.jobs.ExecutionMode;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.Result;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;
import ro.cosu.vampires.server.values.resources.Configuration;
import ro.cosu.vampires.server.values.resources.ResourceDemand;
import ro.cosu.vampires.server.values.resources.ResourceDescription;
import scala.concurrent.duration.Duration;

import static com.google.common.collect.ImmutableList.of;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static ro.cosu.vampires.server.values.jobs.ExecutionMode.FULL;
import static ro.cosu.vampires.server.values.jobs.ExecutionMode.SAMPLE;

public class ExecutionActorTest extends AbstractActorTest {

    private Execution getExec(ExecutionMode mode) {

        ImmutableList<ResourceDemand> resourceDemands = of(
                ResourceDemand.builder().count(2).resourceDescription(ResourceDescription.builder()
                        .provider(Resource.ProviderType.MOCK).resourceType("foo")
                        .cost(0.1)
                        .build()).build()
        );

        Configuration configuration = Configuration.builder().resources(resourceDemands).description("foo").build();
        Workload workload = Workload.builder()
                .format("%d")
                .url("")
                .sequenceStart(0).sequenceStop(100).task("bar").build();

        return Execution.builder().configuration(configuration).type(mode)
                .info(ExecutionInfo.empty())
                .workload(workload).build();
    }

    @Test
    public void testDispatchOfJob() {

        new TestKit(system) {
            {
                // create a test probe
                final TestKit workProbe = new TestKit(system);

                final ActorRef executionActor = system.actorOf(ExecutionActor.props(getExec(FULL)));

                // this is a job request
                Job job = Job.builder()
                        .computation(Computation.builder().command("1").build())
                        .hostMetrics(Metrics.empty())
                        .result(Result.empty())
                        .build();
                // send it
                executionActor.tell(job, workProbe.testActor());

                // expect a full job back
                workProbe.expectMsgClass(Job.class);
            }
        };
    }

    @Test
    public void testDispatchOfConfig() {
        new TestKit(system) {
            {
                final TestKit workProbe = new TestKit(system);

                final ActorRef executionActor = system.actorOf(ExecutionActor.props(getExec(FULL)));

                ClientInfo clientInfo = getClientInfo();

                executionActor.tell(clientInfo, workProbe.testActor());

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
    public void testShutdown() throws InterruptedException {

        new TestKit(system) {
            {
                Execution exec = getExec(ExecutionMode.FULL);
                // create a test probe
                final TestKit workProbe = new TestKit(system);

                final ActorRef executionActor = system.actorOf(ExecutionActor.props(exec));

                workProbe.watch(executionActor);

                executionActor.tell(ResourceControl.Shutdown.create(), workProbe.testActor());

                ResourceInfo resourceInfo = workProbe.expectMsgClass(ResourceInfo.class);

                assertThat(resourceInfo.status(), is(Resource.Status.STOPPED));


            }
        };
    }

    @Test
    public void testDispatchSample() {
        // create sample execution and expect that after 30 job requests (default sample val)
        // we'll get backoff  jobs
        new TestKit(system) {
            {
                // create a test probe
                final TestKit workProbe = new TestKit(system);

                final ActorRef executionActor = system.actorOf(ExecutionActor.props(getExec(SAMPLE)));

                for (int i = 0; i < 31; i++) {
                    Job job = Job.builder()
                            .computation(Computation.builder().command("1").build())
                            .hostMetrics(Metrics.empty())
                            .result(Result.empty())
                            .build();
                    executionActor.tell(job, workProbe.testActor());

                    Job job1 = workProbe.expectMsgClass(Job.class);
                    workProbe.lastSender().tell(job1.withResult(Result.empty()), workProbe.testActor());
                }

                Job job = Job.builder()
                        .computation(Computation.builder().command("1").build())
                        .hostMetrics(Metrics.empty())
                        .result(Result.empty())
                        .build();
                executionActor.tell(job, workProbe.testActor());
                Job job1 = workProbe.expectMsgClass(Job.class);
                assertThat(job1.computation().id().equals("BACKOFF"), is(true));
            }
        };
    }


}

