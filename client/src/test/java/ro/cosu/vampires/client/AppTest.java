package ro.cosu.vampires.client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import kamon.sigar.SigarProvisioner;
import org.junit.Test;
import ro.cosu.vampires.client.monitoring.CpuSource;
import ro.cosu.vampires.client.monitoring.MonitoringModule;

public class AppTest {

    @Test
    public void testName() throws Exception {
        SigarProvisioner.provision();

        Injector injector = Guice.createInjector(new MonitoringModule());
        CpuSource source = injector.getInstance(CpuSource.class);


    }
}
