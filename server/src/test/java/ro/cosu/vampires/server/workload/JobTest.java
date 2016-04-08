package ro.cosu.vampires.server.workload;

import org.junit.Test;

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
