package ro.cosu.vampires.client.monitoring;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostInfo {
    static final Logger LOG = LoggerFactory.getLogger(HostInfo.class);
    public static int getParallel(){
        Sigar sigar = new Sigar();
        try {
            CpuInfo[] cpuInfo = sigar.getCpuInfoList();
            return cpuInfo[0].getTotalCores();
        } catch (SigarException e) {
            LOG.error("sigar error: {}", e);
        }
        return  1;

    }
}
