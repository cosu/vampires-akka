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
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.executors.ExecutorMetricsCollector;

import javax.ws.rs.ProcessingException;

public class DockerModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(DockerModule.class);

    @Override
    protected void configure() {
        bind(Executor.class).to(DockerExecutor.class);
        bind(ExecutorMetricsCollector.class).to(DockerExecutorMetricsCollector.class);
    }

    @Provides
    @Named("cpuCount")
    private int provideCpuCount(DockerClient dockerClient) {
        int cpuCount = 0;
        try {
            final Info exec = dockerClient.infoCmd().exec();
            cpuCount = exec.getNCPU();
        } catch (ProcessingException e) {
            LOG.error("failed to get docker cpu count : {}", e.getMessage());
        }
        return cpuCount;
    }

    @Provides
    private DockerClient provideDockerClient(@Named("Config") Config config) {
        Preconditions.checkArgument(config.hasPath("docker.uri"), "missing docker uri on config");
        String uri = config.getString("docker.uri");

//        DockerCmdExecFactoryImpl dockerCmdExecFactory = new DockerCmdExecFactoryImpl()
//                .withReadTimeout(10000)
//                .withConnectTimeout(2000)
//                .withMaxTotalConnections(100)
//                .withMaxPerRouteConnections(10);

        DockerClientConfig dockerClientConfig = DockerClientConfig.createDefaultConfigBuilder()
                .withUri(uri)
//                .withDockerCertPath(certPath)
                .build();

        return DockerClientBuilder
                .getInstance(dockerClientConfig)
//                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();
    }
}
