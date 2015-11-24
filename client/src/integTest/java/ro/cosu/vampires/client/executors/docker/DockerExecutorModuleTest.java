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

public class DockerExecutorModuleTest {
    @Test
    public void testDockerStart() throws Exception {
        Injector injector = Guice.createInjector(new DockerExecutorModule(ConfigFactory.load().getConfig("vampires")));

        final Executor dockerExecutor = injector.getInstance(Executor.class);
        if (dockerExecutor.isAvailable()) {

            Result execute = dockerExecutor.execute(Computation.builder().command("wget http://ipv4.download" +
                    ".thinkbroadband.com/100MB.zip").build());

            assertThat(execute.duration(), not(0));
        }

    }
}
