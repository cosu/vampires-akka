package ro.cosu.vampires.client.monitoring;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import com.codahale.metrics.MetricRegistry;

import org.hyperic.sigar.Sigar;

public class MonitoringModule extends AbstractModule {

    private final MetricRegistry metricRegistry;
    private final Sigar sigar;

    public MonitoringModule(MetricRegistry metricRegistry, Sigar sigar) {

        this.metricRegistry = metricRegistry;
        this.sigar = sigar;
    }

    @Override
    protected void configure() {

        Multibinder<Source> sourceMultibinder = Multibinder.newSetBinder(binder(), Source.class);

        sourceMultibinder.addBinding().to(CpuSource.class).asEagerSingleton();
        sourceMultibinder.addBinding().to(NetworkSource.class).asEagerSingleton();
        sourceMultibinder.addBinding().to(HostSource.class).asEagerSingleton();

        bind(Sigar.class).toInstance(sigar);
        bind(MetricRegistry.class).toInstance(metricRegistry);
    }
}
