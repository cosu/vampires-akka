package ro.cosu.vampires.client;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class ExecutorTest {


    @Test
    public void testNoCommand() throws Exception {
        assertThat(Executor.execute("bla").getExitCode(), is(-1));
    }

    @Test
    public void testExecuteFail() throws Exception {
        assertThat(Executor.execute("cat /dev/null1").getExitCode(), is(not(0)));
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        assertThat(Executor.execute("cat /dev/null").getExitCode(), is(0));
    }
}