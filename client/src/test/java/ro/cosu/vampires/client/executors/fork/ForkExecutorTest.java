package ro.cosu.vampires.client.executors.fork;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.server.workload.Computation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class ForkExecutorTest {


    private Executor getFork(){
        Injector injector = Guice.createInjector(new ForkExecutorModule(ConfigFactory.load().getConfig("vampires")));

        return injector.getInstance(Executor.class);

    }

    @Test
    public void testNoCommand() throws Exception {
        Computation computation = Computation.builder().id("test").command("bla").build();

        Executor executor = getFork();

        assertThat(executor.execute(computation).exitCode(), not(0));
    }

    @Test
    public void testExecuteFail() throws Exception {

        Computation computation = Computation.builder().id("test").command("cat /dev/null1").build();

        Executor executor = getFork();

        assertThat(executor.execute(computation).exitCode(), is(not(0)));
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        Computation computation = Computation.builder().id("test").command("cat /dev/null").build();

        Executor executor = getFork();

        assertThat(executor.execute(computation).exitCode(), is(0));
    }

    @Test
    public void testGetNCPU() throws Exception {

        Executor executor = getFork();
        assertThat(executor.getNCpu(), not(0));

    }
}
