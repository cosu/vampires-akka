/*
 * Created by IntelliJ IDEA.
 * User: cdumitru
 * Date: 25/10/15
 * Time: 23:51
 */
package ro.cosu.vampires.client.executors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.typesafe.config.Config;

public class ExecutorsModule extends AbstractModule {

    private final Config config;

    @Provides
    @Named("Config")
    Config provideConfig() {
        return this.config;
    }

    public ExecutorsModule(Config config) {
        this.config = config;
    }

    protected void configure() {
        MapBinder<Executor.Type, Executor> mapbinder
                = MapBinder.newMapBinder(binder(), Executor.Type.class, Executor.class);

        mapbinder.addBinding(Executor.Type.COMMAND).to(DockerExecutor.class);
        mapbinder.addBinding(Executor.Type.DOCKER).to(CommandExecutor.class);
    }

    @Provides
    DockerClient provideDockerCLient() {
        String uri = config.getString("docker.uri");
        String certPath = config.getString("docker.certPath");

        DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder()
                .withUri(uri)
                .withDockerCertPath(certPath)
                .build();

        return DockerClientBuilder.getInstance(config).build();

    }
}
