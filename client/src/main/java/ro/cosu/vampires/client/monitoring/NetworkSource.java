package ro.cosu.vampires.client.monitoring;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.util.Arrays;

import static com.codahale.metrics.MetricRegistry.name;

public class NetworkSource implements Source {


    @Inject
    Sigar sigar;
    @Inject
    MetricRegistry metricRegistry;

    public void register() throws SigarException {
        Arrays.stream(sigar.getNetInterfaceList()).forEach(iface -> {
            metricRegistry.register(name(NetworkSource.class, "tx-bytes", iface), this.txBytesGague(iface));
            metricRegistry.register(name(NetworkSource.class, "rx-bytes", iface), this.rxBytesGague(iface));
        });
    }


    protected Gauge<Long> txBytesGague(final String iface)
    {
        return () -> {
            try
            {
                NetInterfaceStat nis = sigar.getNetInterfaceStat(iface);
                return nis.getTxBytes();
            }
            catch (SigarException e)
            {
            }
            return null;
        };
    }

    protected Gauge<Long> rxBytesGague(final String iface)
    {
        return () -> {
            try
            {
                NetInterfaceStat nis = sigar.getNetInterfaceStat(iface);
                return nis.getRxBytes();
            }
            catch (SigarException e)
            {
            }
            return null;
        };
    }
}
