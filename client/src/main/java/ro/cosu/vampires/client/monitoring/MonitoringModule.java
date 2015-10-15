package ro.cosu.vampires.client.monitoring;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import org.hyperic.sigar.Sigar;

public class MonitoringModule extends AbstractModule {
    private final MetricRegistry metricRegistry = new MetricRegistry();
    final Sigar sigar = new Sigar();

    @Override
    protected void configure() {

        bind(MetricRegistry.class).toInstance(metricRegistry);
        bind(Sigar.class).toInstance(sigar);

    }
}
