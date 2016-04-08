package ro.cosu.vampires.client.executors.fork;

import com.typesafe.config.Config;

import ro.cosu.vampires.client.executors.AbstractExecutorModule;

public class ForkExecutorModule extends AbstractExecutorModule {
    public ForkExecutorModule(Config config) {
        super(config);
    }

    @Override
    protected void configure() {
        install(new ForkModule());
    }
}
