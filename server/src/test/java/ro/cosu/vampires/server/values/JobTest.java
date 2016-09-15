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

package ro.cosu.vampires.server.values;

import org.junit.Test;

import ro.cosu.vampires.server.values.jobs.Computation;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.JobStatus;
import ro.cosu.vampires.server.values.jobs.Result;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JobTest {
    @Test
    public void testWorkloadBuilder() throws Exception {

        Job build = getJob();
        assertThat(build.computation().id(), is("10"));
    }

    private Job getJob() {
        return Job.builder()
                .computation(Computation.builder().command("test").id("10").build())
                .hostMetrics(Metrics.empty())
                .result(Result.empty())
                .build();
    }

    @Test
    public void testWithResult() throws Exception {
        Job job = getJob().withResult(Result.empty());
        assertThat(job.status(), is(JobStatus.EXECUTED));
    }

    @Test
    public void testWithMetrics() throws Exception {
        Job job = getJob().withHostMetrics(Metrics.empty());
        assertThat(job.status(), is(JobStatus.COMPLETE));
    }

    @Test
    public void testWithBackoff() throws Exception {
        Job job = Job.backoff(5);
        assertThat(job.computation().command(), containsString("sleep 5"));
    }
}
