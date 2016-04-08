package ro.cosu.vampires.server.workload.schedulers;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import ro.cosu.vampires.server.workload.Job;

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
