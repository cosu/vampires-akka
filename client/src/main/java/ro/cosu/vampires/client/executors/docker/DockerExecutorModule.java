package ro.cosu.vampires.client.executors.docker;

import com.typesafe.config.Config;
import ro.cosu.vampires.client.executors.AbstractExecutorModule;

public class DockerExecutorModule extends AbstractExecutorModule {
    public DockerExecutorModule(Config config) {
        super(config);
    }

    @Override
    protected void configure() {
        install( new DockerModule());
    }
}
