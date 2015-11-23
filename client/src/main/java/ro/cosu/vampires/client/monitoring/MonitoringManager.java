package ro.cosu.vampires.client.monitoring;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import kamon.sigar.SigarProvisioner;
import org.hyperic.sigar.Sigar;

import java.util.Set;

public class MonitoringManager {
    private final Set<Source> sources;

    @Inject
    public MonitoringManager(Set<Source> sources) {
        this.sources = sources;
    }

    public void register() {
        sources.stream().forEach(Source::register);
    }

    public static MetricRegistry getMetricRegistry() throws Exception {
        SigarProvisioner.provision();
        Sigar sigar = new Sigar();
        MetricRegistry metricRegistry = new MetricRegistry();

        Injector injector = Guice.createInjector(new MonitoringModule(metricRegistry, sigar));

        injector.getInstance(MonitoringManager.class).register();
        return metricRegistry;

    }
}
