package ro.cosu.vampires.client.executors.docker;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import ro.cosu.vampires.client.allocation.CpuAllocator;
import ro.cosu.vampires.client.allocation.FixedCpuSetAllocator;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.executors.ExecutorMetricsCollector;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Metrics;
import ro.cosu.vampires.server.workload.Result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

/**
 * Created on 11-2-16.
 */
public class DockerExecutorTest {
    @Test
    public void testDockerStart() {
        Injector injector = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Executor.class).to(DockerExecutor.class);
                    }

                    @Provides
                    @Named("cpuCount")
                    private int provideCpuCount() {
                        return 1;
                    }

                    @Provides
                    @Named("Config")
                    private Config provideConfig() {
                        Map<String, String> config = Maps.newHashMap();
                        config.put("docker.image", "foo");
                        return ConfigFactory.parseMap(config);
                    }

                    @Provides
                    @Singleton
                    private CpuAllocator provideCpuAllocator() {
                        return FixedCpuSetAllocator.builder()
                                .cpuSetSize(1)
                                .totalCpuCount(1)
                                .build();
                    }

                    @Provides
                    private DockerClient provideDockerClient() throws InterruptedException {

                        DockerClient mock = Mockito.mock(DockerClient.class, RETURNS_DEEP_STUBS);
                        LogContainerCmd logContainerCmdMock = Mockito.mock(LogContainerCmd.class);

                        //mockito has some trouble keeping up with the fluent interface so we mock the chain by hand
                        DockerExecutor.DockerLogResultCallback resultCallbackMock =
                                Mockito.mock(DockerExecutor.DockerLogResultCallback.class, RETURNS_DEEP_STUBS);

                        when(mock.logContainerCmd(anyString()).withStdErr().withStdOut()).thenReturn(logContainerCmdMock);
                        when(logContainerCmdMock.exec(anyObject())).thenReturn(resultCallbackMock);

                        return mock;
                    }

                    @Provides
                    private ExecutorMetricsCollector provideExecutorMetricsCollector() {
                        ExecutorMetricsCollector mock = Mockito.mock(ExecutorMetricsCollector.class);
                        when(mock.getMetrics()).thenReturn(Metrics.empty());
                        return mock;
                    }
                }
        );

        final Executor dockerExecutor = injector.getInstance(Executor.class);

        Result execute = dockerExecutor.execute(Computation.builder().command("ping localhost -c2 ").build());

        assertThat(execute.duration(), not(0));

    }

}