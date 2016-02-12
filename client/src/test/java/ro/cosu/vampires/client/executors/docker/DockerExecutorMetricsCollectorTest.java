package ro.cosu.vampires.client.executors.docker;

import com.github.dockerjava.api.DockerClient;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.junit.Test;
import org.mockito.Mockito;
import ro.cosu.vampires.client.executors.ExecutorMetricsCollector;
import ro.cosu.vampires.server.workload.Metrics;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;


public class DockerExecutorMetricsCollectorTest {
    @Test
    public void testExecutor() throws Exception {
        Injector injector = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ExecutorMetricsCollector.class).to(DockerExecutorMetricsCollector.class);
                    }

                    @Provides
                    private DockerClient provideDockerClient() throws InterruptedException {
                        DockerClient mock = Mockito.mock(DockerClient.class, RETURNS_DEEP_STUBS);

                        return mock;
                    }

                }
        );

        ExecutorMetricsCollector  executorMetricsCollector = injector.getInstance(ExecutorMetricsCollector.class);

        executorMetricsCollector.startMonitoring("foo");
        executorMetricsCollector.stopMonitoring();
        Metrics metrics = executorMetricsCollector.getMetrics();

        assertThat(metrics.metadata().containsKey("docker"), is(true));

    }
}