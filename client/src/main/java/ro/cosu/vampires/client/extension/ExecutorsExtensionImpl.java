package ro.cosu.vampires.client.extension;

import akka.actor.Extension;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.executors.docker.DockerExecutorModule;
import ro.cosu.vampires.client.executors.fork.ForkExecutorModule;
import ro.cosu.vampires.server.workload.ClientConfig;

import java.util.HashMap;
import java.util.Map;

public class ExecutorsExtensionImpl implements Extension {

    public Config vampires;
    private static final Logger LOG = LoggerFactory.getLogger(ExecutorsExtensionImpl.class);
    private Injector injector;
    private Map<String, Integer> executorInfo = new HashMap<>();

    public ExecutorsExtensionImpl(Config config) {
        vampires = config.getConfig("vampires");

        try {
            injector = Guice.createInjector(new DockerExecutorModule(vampires));
            final Executor executor = injector.getInstance(Executor.class);
            Preconditions.checkArgument(executor.isAvailable());
            executorInfo.put(executor.getType().toString(), executor.getNCpu());
        }
        catch (Exception e) {
            LOG.info("docker executor not available");
        }

        try {
            injector = Guice.createInjector(new ForkExecutorModule(vampires));
            final Executor executor = injector.getInstance(Executor.class);
            executorInfo.put(executor.getType().toString(), executor.getNCpu());
        }
        catch (Exception e) {
             LOG.info("fork executor not available");
        }

        LOG.info("available executors: {}", executorInfo);

    }

    public  Executor getExecutor(){
        return injector.getInstance(Executor.class);
    }

    public void configure(ClientConfig config) {

        final Executor.Type executor = Executor.Type.valueOf(config.executor());

        vampires = ConfigFactory.parseString("cpuSetSize="+config.cpuSetSize()).withFallback(vampires);

        if (executor.equals(Executor.Type.DOCKER)) {
            injector = Guice.createInjector(new DockerExecutorModule(vampires));
        }

        if (executor.equals(Executor.Type.FORK)) {
            injector = Guice.createInjector(new ForkExecutorModule(vampires));
        }

        LOG.info("configured {}", config);

        Preconditions.checkArgument(getExecutor().isAvailable());

    }

    public Map<String, Integer> getExecutorInfo() {
        return executorInfo;
    }
}


