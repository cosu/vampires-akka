/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.client.executors.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.google.common.collect.Maps;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.mockito.Mockito;
import ro.cosu.vampires.client.allocation.CpuAllocator;
import ro.cosu.vampires.client.allocation.FixedCpuSetAllocator;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.executors.ExecutorMetricsCollector;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Metrics;
import ro.cosu.vampires.server.workload.Result;

import java.util.Map;

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