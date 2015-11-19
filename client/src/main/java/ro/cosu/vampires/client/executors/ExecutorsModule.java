/*
 * Created by IntelliJ IDEA.
 * User: cdumitru
 * Date: 25/10/15
 * Time: 23:51
 */
package ro.cosu.vampires.client.executors;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.client.allocation.CpuAllocator;
import ro.cosu.vampires.client.allocation.FixedCpuSetAllocator;
import ro.cosu.vampires.client.monitoring.HostInfo;

public class ExecutorsModule extends AbstractModule {
    static final Logger LOG = LoggerFactory.getLogger(ExecutorsModule.class);

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

        install(new DockerModule());
        install(new CommandModule());
    }


    @Provides
    @Singleton
    CpuAllocator provideCpuAllocator() {

        int cpuCount = HostInfo.getAvailableProcs();
        final int cpuSetSize = config.getInt("cpuSetSize");
        LOG.info(" cpucount: {} coutSetSize: {}", cpuCount, cpuSetSize);

        return FixedCpuSetAllocator.builder().cpuSetSize(cpuSetSize).totalCpuCount(cpuCount).build();

    }


}
