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

package ro.cosu.vampires.client;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.cosu.vampires.client.actors.MonitoringActor;
import ro.cosu.vampires.server.values.jobs.Computation;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.JobStatus;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;
import ro.cosu.vampires.server.values.jobs.Result;
import ro.cosu.vampires.server.values.jobs.metrics.Trace;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class MonitoringActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setUp() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void tearDown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testMetrics() throws Exception {

        TestActorRef<MonitoringActor> ref = TestActorRef.create(system, MonitoringActor
                .props(TestUtil.getMetricRegistryMock()));

        Computation computation = Computation.builder().command("test").id("test").build();
        LocalDateTime now = LocalDateTime.now();

        Result result = Result.builder()
                .duration(1)
                .exitCode(0)
                .output(Lists.newLinkedList())
                .trace(Trace.builder()
                        .start(now.minusSeconds(1))
                        .stop(now.plusSeconds(1))
                        .cpuSet(Sets.newHashSet(1)).executor("foo").totalCpuCount(1)
                        .executorMetrics(Metrics.empty())
                        .build())
                .build();

        Job jobWithoutMetrics = Job.builder().computation(computation).result(result)
                .hostMetrics(Metrics.empty())
                .status(JobStatus.EXECUTED)
                .build();

        Future<Object> future = akka.pattern.Patterns.ask(ref, jobWithoutMetrics, 1000);

        Job job = (Job) Await.result(future, Duration.create("1 seconds"));

        assertThat(job.hostMetrics().metadata().keySet().size(), not(0));
    }

    @Test
    public void testReplyToMetrics() throws Exception {
        TestActorRef<MonitoringActor> ref = TestActorRef.create(system, MonitoringActor
                .props(TestUtil.getMetricRegistryMock()));

        Future<Object> future = akka.pattern.Patterns.ask(ref, Metrics.empty(), 1000);

        Metrics metrics = (Metrics) Await.result(future, Duration.create("1 seconds"));

        assertThat(metrics.metadata().keySet().size(), not(0));

    }
}
