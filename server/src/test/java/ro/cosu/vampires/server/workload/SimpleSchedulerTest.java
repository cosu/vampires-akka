package ro.cosu.vampires.server.workload;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;


public class SimpleSchedulerTest {

    private Scheduler scheduler;
    @Before
    public void setUp() throws Exception {
        List<Job> jobs = Arrays.asList(Job.empty());
        scheduler = new SimpleScheduler(jobs, 10, 10);

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
    public void testGetBackoff() throws  Exception {
        scheduler.getJob("foo");
        Job foo = scheduler.getJob("foo");
        assertThat(foo.computation().id(), is(Computation.BACKOFF));
    }

}