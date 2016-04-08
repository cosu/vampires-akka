package ro.cosu.vampires.client.monitoring;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import com.codahale.metrics.MetricRegistry;

import org.hyperic.sigar.Sigar;

import java.util.Set;

import kamon.sigar.SigarProvisioner;

public class MonitoringManager {
    private final Set<Source> sources;

    @Inject
    public MonitoringManager(Set<Source> sources) {
        this.sources = sources;
    }

    public static MetricRegistry getMetricRegistry(Sigar sigar) throws Exception {
        MetricRegistry metricRegistry = new MetricRegistry();

        Injector injector = Guice.createInjector(new MonitoringModule(metricRegistry, sigar));

        injector.getInstance(MonitoringManager.class).register();
        return metricRegistry;

    }

    public static Sigar getSigar() throws Exception {
        SigarProvisioner.provision();
        return new Sigar();
    }

    public static MetricRegistry getMetricRegistry() throws Exception {
        return getMetricRegistry(getSigar());
    }

    public void register() {
        sources.stream().forEach(Source::register);
    }
}
