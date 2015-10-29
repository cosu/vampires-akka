package ro.cosu.vampires.client.executors;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Ignore;
import org.junit.Test;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class DockerExecutorTest {

    @Test
    @Ignore
    public void testExecute() throws Exception {

        Injector injector = Guice.createInjector(new ExecutorsModule(ConfigFactory.load().getConfig("vampires")));

        ExecutorsManager em= injector.getInstance(ExecutorsManager.class);

        Result execute = em.getProvider("docker").get().execute(Computation.builder().command("echo 1").build());


        assertThat(execute.duration(), not(0));

    }
}
