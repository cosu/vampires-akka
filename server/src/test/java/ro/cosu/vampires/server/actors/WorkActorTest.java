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
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.testkit.TestKit;
import ch.qos.logback.classic.Level;
import ro.cosu.vampires.server.actors.execution.WorkActor;
import ro.cosu.vampires.server.schedulers.Scheduler;
import ro.cosu.vampires.server.schedulers.SimpleScheduler;
import ro.cosu.vampires.server.util.TestAppender;
import ro.cosu.vampires.server.values.jobs.Computation;
import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.ExecutionInfo;
import ro.cosu.vampires.server.values.jobs.ExecutionMode;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.Result;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;
import ro.cosu.vampires.server.values.resources.Configuration;
import scala.concurrent.duration.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class WorkActorTest extends AbstractActorTest {
    private int backOffinterval = 10;

    private Execution getExecution() {
        Configuration configuration = Configuration.builder().resources(ImmutableList.of()).description("foo").build();
        Workload workload = Workload.builder().format("foo").sequenceStart(0).sequenceStop(1).task("bar").build();

        return Execution.builder().configuration(configuration).
                type(ExecutionMode.FULL).info(ExecutionInfo.empty()).workload(workload).build();
    }

    private Scheduler getScheduler() {
        return new SimpleScheduler(getExecution().workload().jobs(), 100, TimeUnit.SECONDS, backOffinterval);
    }
    @Test
    public void testWork() {

        new TestKit(system) {
            {
                // create a test probe
                final TestKit workProbe = new TestKit(system);

                // create a work actor
                final Props props = WorkActor.props(getScheduler());
                final ActorRef workActor = system.actorOf(props, "workactor");
                workProbe.watch(workActor);
                Job job = Job.builder()
                        .computation(Computation.builder().command("1").build())
                        .hostMetrics(Metrics.empty())
                        .result(Result.empty())
                        .build();
                // send the job request
                workActor.tell(job, workProbe.testActor());

                // receive the first job
                Job receivedJob = workProbe.expectMsgClass(Job.class);

                // send the processed job
                workActor.tell(receivedJob.withResult(Result.empty()).withHostMetrics(Metrics.empty()), workProbe.testActor());

                // receive a 2nd job
                receivedJob = workProbe.expectMsgClass(Job.class);

                // send the processed job
                workActor.tell(receivedJob.withResult(Result.empty()).withHostMetrics(Metrics.empty()), workProbe.testActor());

                // receive the 3rd job
                receivedJob = workProbe.expectMsgClass(Job.class);

                // it should be a backoff
                assertThat(receivedJob.computation(), is(Computation.backoff(backOffinterval)));

                // work actor auto-shuts down{
                workProbe.expectMsgClass(Terminated.class);


            }
        };
    }

    @Test
    public void unhandled() throws Exception {
        TestAppender.clear();
        new TestKit(system) {
            {
                // create a test probe
                final TestKit workProbe = new TestKit(system);

                // create a work actor
                final Props props = WorkActor.props(getScheduler());
                final ActorRef workActor = system.actorOf(props);
                workProbe.watch(workActor);
                // send it a bogus message
                workActor.tell(new Object(), ActorRef.noSender());
                workActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
                workProbe.expectTerminated(workActor, Duration.apply("1 second"));
            }
        };
        // logging might be async
        Thread.sleep(50);
        // expect to have something in the logs
        assertThat(TestAppender.events.get(Level.WARN).size(), is(1));
    }
}
