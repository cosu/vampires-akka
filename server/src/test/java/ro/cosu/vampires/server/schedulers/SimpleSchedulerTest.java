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

package ro.cosu.vampires.server.schedulers;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ro.cosu.vampires.server.values.jobs.Computation;
import ro.cosu.vampires.server.values.jobs.Job;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;


public class SimpleSchedulerTest {

    private SimpleScheduler scheduler;

    @Before
    public void setUp() throws Exception {
        List<Job> jobs = Collections.singletonList(Job.empty());
        scheduler = new SimpleScheduler(jobs, 10, TimeUnit.MILLISECONDS, 1);
    }

    @Test
    public void testGetJob() throws Exception {
        Job foo = scheduler.getJob("foo");
        assertThat(foo, notNullValue());
    }

    @Test
    public void testMarkDone() throws Exception {
        Job foo = scheduler.getJob("foo");
        scheduler.markDone(foo);
        assertThat(scheduler.isDone(), is(true));
    }

    @Test
    public void testGetBackoff() throws Exception {
        scheduler.getJob("foo");
        Job foo = scheduler.getJob("foo");
        assertThat(foo.computation().id(), is(Computation.BACKOFF));
    }

    @Test
    public void testEviction() throws Exception {
        Job first = scheduler.getJob("foo");
        Thread.sleep(200);

        scheduler.getPendingJobs().cleanUp();
        Job second = scheduler.getJob("bar");

        assertThat(first.id(), is(second.id()));

    }

}