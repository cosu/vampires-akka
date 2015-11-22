package ro.cosu.vampires.client.executors.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.DockerCmdExecFactoryImpl;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import ro.cosu.vampires.client.executors.Executor;

public class DockerModule extends AbstractModule{

    @Override
    protected void configure() {
        
        bind(Executor.class).to(DockerExecutor.class);
    }

    @Provides
    DockerClient provideDockerClient(@Named("Config") Config config) {
        Preconditions.checkArgument(config.hasPath("docker.uri"), "missing docker uri on config");
        Preconditions.checkArgument(config.hasPath("docker.certPath"), "missing docker certpath");

        String uri = config.getString("docker.uri");
        String certPath = config.getString("docker.certPath");

        DockerCmdExecFactoryImpl dockerCmdExecFactory = new DockerCmdExecFactoryImpl()
                .withReadTimeout(1000)
                .withConnectTimeout(1000)
                .withMaxTotalConnections(100)
                .withMaxPerRouteConnections(10);

        DockerClientConfig dockerClientConfig = DockerClientConfig.createDefaultConfigBuilder()
                .withUri(uri)
                .withDockerCertPath(certPath)
                .build();

        return DockerClientBuilder.getInstance(dockerClientConfig)
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();
    }


}
