package ro.cosu.vampires.client.executors.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.DockerCmdExecFactoryImpl;
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

public class DockerModule extends AbstractModule{
    static final Logger LOG = LoggerFactory.getLogger(DockerModule.class);
    @Override
    protected void configure() {
        
        bind(Executor.class).to(DockerExecutor.class);

        bind(ExecutorMetricsCollector.class).to(DockerExecutorMetricsCollector.class);
    }

    @Provides
    @Named("cpuCount")
    int provideCpuCount(DockerClient dockerClient){
        int cpuCount = 0;
        try {
            final Info exec = dockerClient.infoCmd().exec();
            cpuCount = exec.getNCPU();
        }
        catch (ProcessingException e){
            LOG.error("failed to get docker cpu count : {}", e.getMessage());
        }
        return cpuCount;
    }

    @Provides
    DockerClient provideDockerClient(@Named("Config") Config config) {
        Preconditions.checkArgument(config.hasPath("docker.uri"), "missing docker uri on config");
//        Preconditions.checkArgument(config.hasPath("docker.certPath"), "missing docker certpath");

        String uri = config.getString("docker.uri");
//        String certPath = config.getString("docker.certPath");

        DockerCmdExecFactoryImpl dockerCmdExecFactory = new DockerCmdExecFactoryImpl()
                .withReadTimeout(10000)
                .withConnectTimeout(2000)
                .withMaxTotalConnections(100)
                .withMaxPerRouteConnections(10);

        DockerClientConfig dockerClientConfig = DockerClientConfig.createDefaultConfigBuilder()
                .withUri(uri)
//                .withDockerCertPath(certPath)
                .build();


        return DockerClientBuilder
                .getInstance(dockerClientConfig)
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();
    }


}
