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

import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.Metrics;
import ro.cosu.vampires.server.workload.Result;
import ro.cosu.vampires.server.workload.Workload;

public class WorkActorTest extends AbstractActorTest {
    @Test
    public void testWork() {
        new JavaTestKit(system) {
            {
                // create a test probe
                final JavaTestKit workProbe = new JavaTestKit(system);

                Workload workload = Workload.builder().sequenceStart(1).sequenceStop(2)
                        .url("")
                        .format("")
                        .task("1").build();
                // create a work actor
                final Props props = WorkActor.props(workload, ExecutionMode.FULL);
                final ActorRef forwarder = system.actorOf(props, "workactor");

                Job job = Job.builder()
                        .computation(Computation.builder().command("1").build())
                        .hostMetrics(Metrics.empty())
                        .result(Result.empty())
                        .build();

                forwarder.tell(job, workProbe.getRef());

                workProbe.expectMsgClass(Job.class);

            }
        };
    }
}
