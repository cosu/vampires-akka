package ro.cosu.vampires.client;

import org.junit.Test;
import ro.cosu.vampires.client.executors.CommandExecutor;
import ro.cosu.vampires.server.workload.Computation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class CommandExecutorTest {


    @Test
    public void testNoCommand() throws Exception {
        Computation computation = Computation.builder().id("test").command("bla").build();

        CommandExecutor executor = new CommandExecutor();

        assertThat(executor.execute(computation).exitCode(), is(-1));
    }

    @Test
    public void testExecuteFail() throws Exception {

        Computation computation = Computation.builder().id("test").command("cat /dev/null1").build();

        CommandExecutor executor = new CommandExecutor();

        assertThat(executor.execute(computation).exitCode(), is(not(0)));
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        Computation computation = Computation.builder().id("test").command("cat /dev/null").build();
        CommandExecutor executor = new CommandExecutor();

        assertThat(executor.execute(computation).exitCode(), is(0));
    }
}
