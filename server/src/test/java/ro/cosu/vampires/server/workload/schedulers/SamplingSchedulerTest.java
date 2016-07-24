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

package ro.cosu.vampires.server.workload.schedulers;

import org.junit.Before;
import org.junit.Test;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.schedulers.SamplingScheduler;
import ro.cosu.vampires.server.schedulers.Scheduler;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class SamplingSchedulerTest {

    private Scheduler scheduler;

    @Before
    public void setUp() throws Exception {
        List<Job> jobs = Arrays.asList(Job.empty().withCommand("foo"), Job.empty().withCommand("bar"));
        scheduler = new SamplingScheduler(jobs, 1, 1, 10);
    }

    @Test
    public void testGetJob() throws Exception {
        List<Job> jobsClient1 = Arrays.asList(scheduler.getJob("client1"), scheduler.getJob("client1"));
        List<Job> jobsClient2 = Arrays.asList(scheduler.getJob("client2"), scheduler.getJob("client2"));
        assertThat(scheduler.isDone(), is(false));

        jobsClient1.stream().map(job -> job.from("client1")).forEach(scheduler::markDone);
        jobsClient2.stream().map(job -> job.from("client2")).forEach(scheduler::markDone);
        assertThat(scheduler.isDone(), is(true));
    }
}
