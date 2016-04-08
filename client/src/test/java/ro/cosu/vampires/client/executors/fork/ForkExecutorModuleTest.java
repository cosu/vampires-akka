package ro.cosu.vampires.client.executors.fork;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class ForkExecutorModuleTest {

    @Test
    public void testStart() throws Exception {
        Injector injector = Guice.createInjector(new ForkExecutorModule(ConfigFactory.load().getConfig("vampires")));

        final Executor forkExecutor = injector.getInstance(Executor.class);

        Result execute = forkExecutor.execute(Computation.builder().command("echo 1").build());

        assertThat(execute.duration(), not(0));

    }
}
