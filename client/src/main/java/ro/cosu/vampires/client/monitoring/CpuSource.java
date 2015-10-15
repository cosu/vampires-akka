package ro.cosu.vampires.client.monitoring;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.util.Arrays;

import static com.codahale.metrics.MetricRegistry.name;

public class CpuSource implements Source {

    @Inject
    Sigar sigar;
    @Inject
    MetricRegistry metricRegistry;

    public void register() throws SigarException {
        Arrays.stream(sigar.getCpuList()).forEach(iface -> {
            metricRegistry.register(name(CpuSource.class, "cpu-total-usage"),this.cpuTotalUsageGague() );
            metricRegistry.register(name(CpuSource.class, "cpu-wait-usage"), this.cpuWaitUsageGague() );
            metricRegistry.register(name(CpuSource.class, "cpu-user-usage"), this.cpuUserUsageGague());
        });
    }


    protected Gauge<Double> cpuUserUsageGague()
    {
        return () -> {
            try
            {
                CpuPerc cpuPc = sigar.getCpuPerc();
                return round(cpuPc.getUser());
            }
            catch (SigarException e)
            {
            }
            return null;
        };
    }

    protected Gauge<Double> cpuWaitUsageGague()
    {
        return () -> {
            try
            {
                CpuPerc cpuPc = sigar.getCpuPerc();
                return round(cpuPc.getWait());
            }
            catch (SigarException e)
            {
            }
            return null;
        };
    }

    protected Gauge<Double> cpuTotalUsageGague()
    {
        return () -> {
            try
            {
                CpuPerc cpuPc = sigar.getCpuPerc();
                return round(cpuPc.getCombined());
            }
            catch (SigarException e)
            {
            }
            return null;
        };
    }

    protected static double round(double d)
    {
        return (((double) Math.round(d * 1000)) / 1000D);
    }

}
