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

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.Metrics;
import ro.cosu.vampires.server.workload.Result;
import ro.cosu.vampires.server.workload.Workload;
import scala.concurrent.duration.Duration;

public class ResultActorTest extends AbstractActorTest {

    private Execution getExec() {
        Configuration configuration = Configuration.builder().resources(ImmutableList.of()).description("foo").build();
        Workload workload = Workload.builder().format("foo").sequenceStart(0).sequenceStop(2).task("bar").build();

        return Execution.builder().configuration(configuration).
                type(ExecutionMode.FULL).workload(workload).build();

    }
    @Test
    public void testShutdown() throws Exception {


        final TestProbe testProbe = new TestProbe(system);
        final TestActorRef<ResultActor> sut = TestActorRef.create(system, ResultActor.props(getExec()));
        testProbe.watch(sut);
        sut.tell(new ResourceControl.Shutdown(), ActorRef.noSender());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), Terminated.class);

    }


    @Test
    public void testShutdownWhenAllResults() throws Exception {

        final TestProbe testProbe = new TestProbe(system);
        final TestActorRef<ResultActor> sut = TestActorRef.create(system, ResultActor.props(getExec()));
        testProbe.watch(sut);
        Job job = Job.builder()
                .computation(Computation.builder().command("1").build())
                .hostMetrics(Metrics.empty())
                .result(Result.empty())
                .build();
        sut.tell(job, testProbe.ref());

        Object o = testProbe.receiveOne(Duration.create("1s"));


    }

}
