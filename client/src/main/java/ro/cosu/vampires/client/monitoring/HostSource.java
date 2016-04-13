/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.client.monitoring;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import org.hyperic.sigar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codahale.metrics.MetricRegistry.name;

public class HostSource implements Source {
    private static final Logger LOG = LoggerFactory.getLogger(CpuSource.class);

    private final static String NAME = "host";

    @Inject
    private Sigar sigar;
    @Inject
    private MetricRegistry metricRegistry;

    public void register() {
        metricRegistry.register(name(getName(), "hostname"), this.hostNameGague());
        metricRegistry.register(name(getName(), "ram"), this.ramGauge());
        metricRegistry.register(name(getName(), "cpu-vendor"), this.cpuVendorGague());
        metricRegistry.register(name(getName(), "cpu-model"), this.cpuModelGague());
        metricRegistry.register(name(getName(), "cpu-sockets"), this.cpuSocketsGague());
        metricRegistry.register(name(getName(), "cpu-cores"), this.cpuCoresGague());
        metricRegistry.register(name(getName(), "cpu-frequency"), this.cpuFrequencyGague());
        metricRegistry.register(name(getName(), "cpu-cache"), this.cpuCacheGague());


        try {
            for (String iface : sigar.getNetInterfaceList()) {
                metricRegistry.register(name(getName(), "hardware-address", iface), this.getHWAddress(iface));
                metricRegistry.register(name(getName(), "address", iface), this.getAddress(iface));
                metricRegistry.register(name(getName(), "netmask", iface), this.getNetMask(iface));
                metricRegistry.register(name(getName(), "type", iface), this.getType(iface));

            }
        } catch (SigarException e) {
            LOG.error("network interface list failed ", e);

        }

        LOG.debug("host info source");
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Gauge<Long> ramGauge() {
        return () -> {
            try {
                Mem m = sigar.getMem();
                return m.getTotal();
            } catch (SigarException e) {
                LOG.error("ram metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<String> cpuVendorGague() {
        return () -> {
            try {
                CpuInfo[] cpuInfo = sigar.getCpuInfoList();
                System.out.println(cpuInfo[0].toMap());

                return cpuInfo[0].getVendor();
            } catch (SigarException e) {
                LOG.error("cpu vendor register failed ", e);
            }
            return null;
        };
    }


    private Gauge<Integer> cpuSocketsGague() {
        return () -> {
            try {
                CpuInfo[] cpuInfo = sigar.getCpuInfoList();
                return cpuInfo[0].getTotalSockets();
            } catch (SigarException e) {
                LOG.error("cpu sockets register failed ", e);
            }
            return null;
        };
    }

    private Gauge<String> cpuModelGague() {
        return () -> {
            try {
                CpuInfo[] cpuInfo = sigar.getCpuInfoList();
                return cpuInfo[0].getModel();
            } catch (SigarException e) {
                LOG.error("cpu vendor register failed ", e);
            }
            return null;
        };
    }

    private Gauge<Integer> cpuFrequencyGague() {
        return () -> {
            try {
                CpuInfo[] cpuInfo = sigar.getCpuInfoList();
                return cpuInfo[0].getMhz();
            } catch (SigarException e) {
                LOG.error("cpu vendor register failed ", e);
            }
            return null;
        };
    }


    private Gauge<Integer> cpuCoresGague() {
        return () -> {
            try {
                CpuInfo[] cpuInfo = sigar.getCpuInfoList();
                return cpuInfo[0].getTotalCores();
            } catch (SigarException e) {
                LOG.error("cpu vendor register failed ", e);
            }
            return null;
        };
    }

    private Gauge<String> hostNameGague() {
        return () -> {
            try {
                return sigar.getFQDN();
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<String> getHWAddress(final String iface) {
        return () -> {
            try {
                NetInterfaceConfig nic = sigar.getNetInterfaceConfig(iface);
                return nic.getHwaddr();
            } catch (SigarException e) {
                LOG.error("network metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<String> getAddress(final String iface) {
        return () -> {
            try {
                NetInterfaceConfig nic = sigar.getNetInterfaceConfig(iface);
                return nic.getAddress();
            } catch (SigarException e) {
                LOG.error("network metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<String> getNetMask(final String iface) {
        return () -> {
            try {
                NetInterfaceConfig nic = sigar.getNetInterfaceConfig(iface);
                return nic.getNetmask();
            } catch (SigarException e) {
                LOG.error("network metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<String> getType(final String iface) {
        return () -> {
            try {
                NetInterfaceConfig nic = sigar.getNetInterfaceConfig(iface);
                return nic.getType();
            } catch (SigarException e) {
                LOG.error("network metric register failed ", e);
            }
            return null;
        };
    }


    private Gauge<Long> cpuCacheGague() {
        return () -> {
            try {
                CpuInfo[] cpuInfo = sigar.getCpuInfoList();
                return cpuInfo[0].getCacheSize();
            } catch (SigarException e) {
                LOG.error("network metric register failed ", e);
            }
            return null;
        };
    }
}


