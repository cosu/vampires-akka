package ro.cosu.vampires.client.executors.docker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class DockerExecutorModuleIT {
    @Test
    public void testDockerStart() {
        Injector injector = Guice.createInjector(new DockerExecutorModule(ConfigFactory.load().getConfig("vampires")));

        final Executor dockerExecutor = injector.getInstance(Executor.class);

        Result execute = dockerExecutor.execute(Computation.builder().command("ping localhost -c2 ").build());

        assertThat(execute.duration(), not(0));

    }
}
