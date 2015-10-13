package ro.cosu.vampires.client;

import kamon.sigar.SigarProvisioner;
import org.hyperic.sigar.Sigar;
import org.junit.Test;

public class AppTest {

    @Test
    public void testName() throws Exception {
        SigarProvisioner.provision();
        final Sigar sigar = new Sigar();
        System.out.println(sigar.getCpu().toMap());
        System.out.println(sigar.getNetInfo().toMap());
        System.out.println(sigar.getNetStat().getAllInboundTotal());
    }
}
