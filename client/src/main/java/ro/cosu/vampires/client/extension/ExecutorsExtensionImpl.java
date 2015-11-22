package ro.cosu.vampires.client.extension;

import akka.actor.Extension;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.executors.docker.DockerExecutorModule;
import ro.cosu.vampires.client.executors.fork.ForkExecutorModule;
import ro.cosu.vampires.server.settings.Settings;

import java.util.Objects;

public class ExecutorsExtensionImpl implements Extension {

    public final Config vampires;
    static final Logger LOG = LoggerFactory.getLogger(Settings.class);
    private Injector injector;
    public ExecutorsExtensionImpl(Config config) {
        vampires = config.getConfig("vampires");

        try {
            injector = Guice.createInjector(new DockerExecutorModule(vampires));
            Preconditions.checkArgument(getExecutor().isAvailable());
        }
        catch (Exception e) {
            LOG.info("can not start docker executor");
            injector = Guice.createInjector(new ForkExecutorModule(vampires));
        }

        Objects.requireNonNull(injector, "failed to init executors config");
    }

    public  Executor getExecutor(){
        return injector.getInstance(Executor.class);
    }

}


