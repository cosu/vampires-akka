package ro.cosu.vampires.client.executors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.DockerCmdExecFactoryImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.typesafe.config.Config;

public class DockerModule extends AbstractModule{

    @Override
    protected void configure() {
        MapBinder<Executor.Type, Executor> mapbinder
                = MapBinder.newMapBinder(binder(), Executor.Type.class, Executor.class);

        mapbinder.addBinding(Executor.Type.DOCKER).to(DockerExecutor.class);
    }
    @Provides
    DockerClient provideDockerCLient(@Named("Config") Config config) {
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
