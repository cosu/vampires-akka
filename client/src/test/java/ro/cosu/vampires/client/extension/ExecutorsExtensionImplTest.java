package ro.cosu.vampires.client.extension;

import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import ro.cosu.vampires.client.executors.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ExecutorsExtensionImplTest {

    @Test
    public void extensionForkDefault() throws Exception {
        ExecutorsExtensionImpl executorsExtension = new ExecutorsExtensionImpl(ConfigFactory.parseString("vampires {}"));

        Executor executor = executorsExtension.getExecutor();
        assertThat(executor.getType(), is(Executor.Type.FORK));
    }

    @Test
    public void extensionForkFallback() throws Exception {
        ExecutorsExtensionImpl executorsExtension = new ExecutorsExtensionImpl(ConfigFactory.parseString("vampires.docker.uri = \"tcp://foo\""));
        Executor executor = executorsExtension.getExecutor();
        assertThat(executor.getType(), is(Executor.Type.FORK));
    }
}