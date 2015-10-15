package ro.cosu.vampires.client.monitoring;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import kamon.sigar.SigarProvisioner;
import org.hyperic.sigar.Sigar;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class MonitoringModuleTest {
    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }
    @Test
    public void testMonitoring() throws Exception {
        SigarProvisioner.provision();
        Sigar sigar = new Sigar();
        MetricRegistry metricRegistry = new MetricRegistry();

        Injector injector = Guice.createInjector(new MonitoringModule(metricRegistry, sigar));

        injector.getInstance(MonitoringManager.class).register();

        assertThat(metricRegistry.getGauges().values().iterator().next().getValue(), notNullValue());



    }
}
