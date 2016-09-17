/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.client.monitoring;

import com.google.inject.Inject;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import static com.codahale.metrics.MetricRegistry.name;

public class HostSource implements Source {
    private static final Logger LOG = LoggerFactory.getLogger(CpuSource.class);

    private final static String NAME = "host";

    private final MetricRegistry metricRegistry;

    private final SystemInfo si;

    @Inject
    public HostSource(MetricRegistry metricRegistry, SystemInfo si) {
        this.metricRegistry = metricRegistry;
        this.si = si;
    }


    public void register() {
        metricRegistry.register(name(getName(), "ram"), ramGauge());
        metricRegistry.register(name(getName(), "cpu-vendor"), cpuVendorGague());
        metricRegistry.register(name(getName(), "cpu-model"), cpuModelGague());
        metricRegistry.register(name(getName(), "cpu-family"), cpuFamily());
        metricRegistry.register(name(getName(), "cpu-stepping"), cpuStepping());
        metricRegistry.register(name(getName(), "cpu-name"), cpuNameGague());
        metricRegistry.register(name(getName(), "cpu-logical-processors"), cpuLogicalProcessorCount());
        metricRegistry.register(name(getName(), "cpu-cores"), cpuPhysicalProcessorCount());
        metricRegistry.register(name(getName(), "cpu-frequency"), cpuFrequencyGague());

        NetworkIF[] netArray = si.getHardware().getNetworkIFs();
        for (NetworkIF iface : netArray) {
            metricRegistry.register(name(getName(), "address", iface.getName()), getAddress(iface));
            metricRegistry.register(name(getName(), "mac", iface.getName()), getHWAddress(iface));
        }

        LOG.debug("host info source");
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Gauge<Long> ramGauge() {
        return () -> si.getHardware().getMemory().getAvailable();
    }

    private Gauge<String> cpuVendorGague() {
        return () -> si.getHardware().getProcessor().getVendor();
    }

    private Gauge<String> cpuFamily() {
        return () -> si.getHardware().getProcessor().getFamily();
    }

    private Gauge<String> cpuStepping() {
        return () -> si.getHardware().getProcessor().getStepping();
    }

    private Gauge<String> cpuModelGague() {
        return () -> si.getHardware().getProcessor().getModel();
    }


    private Gauge<String> cpuNameGague() {
        return () -> si.getHardware().getProcessor().getName();
    }


    private Gauge<Integer> cpuPhysicalProcessorCount() {
        return () -> si.getHardware().getProcessor().getPhysicalProcessorCount();
    }

    private Gauge<Integer> cpuLogicalProcessorCount() {
        return () -> si.getHardware().getProcessor().getLogicalProcessorCount();
    }

    private Gauge<Long> cpuFrequencyGague() {
        return () -> si.getHardware().getProcessor().getVendorFreq();
    }

    private Gauge<String> getAddress(final NetworkIF iface) {
        String addr = "unknown";
        if (iface.getIPv4addr().length > 0) {
            addr = iface.getIPv4addr()[0];
        }
        String finalAddr = addr;
        return () -> finalAddr;
    }

    private Gauge<String> getHWAddress(final NetworkIF iface) {
        return iface::getMacaddr;
    }


}


