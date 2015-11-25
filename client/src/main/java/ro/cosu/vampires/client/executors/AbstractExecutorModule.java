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

public abstract class AbstractExecutorModule extends AbstractModule {
    static final Logger LOG = LoggerFactory.getLogger(AbstractExecutorModule.class);

    private final Config config;

    @Provides
    @Named("Config")
    Config provideConfig() {
        return this.config;
    }


    public AbstractExecutorModule(Config config) {
        this.config = config;
    }

    protected abstract  void configure();


    @Provides
    @Singleton
    CpuAllocator provideCpuAllocator(@Named("cpuCount")  int cpuCount) {
        final int cpuSetSize = config.getInt("cpuSetSize");

        LOG.info(" cpuCount: {} countSetSize: {}", cpuCount, cpuSetSize);

        return FixedCpuSetAllocator.builder()
                .cpuSetSize(cpuSetSize)
                .totalCpuCount(cpuCount)
                .build();

    }


}
