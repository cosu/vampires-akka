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

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.codahale.metrics.MetricRegistry;

import org.hyperic.sigar.Sigar;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Arrays;
import java.util.logging.LogManager;

import kamon.sigar.SigarProvisioner;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

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

    @Test
    public void cpu() throws Exception {
        SigarProvisioner.provision();
        Sigar sigar = new Sigar();
        double[] loadAverage = sigar.getLoadAverage();

        System.out.println(sigar.getCpuPerc());
        System.out.println(sigar.getCpu());
        Arrays.stream(sigar.getCpuList()).forEach(System.out::println);
        Arrays.stream(sigar.getCpuPercList()).forEach(System.out::println);
        Arrays.stream(sigar.getCpuInfoList()).forEach(System.out::println);
        Arrays.stream(loadAverage).forEach(System.out::println);


    }

    @Test
    public void oshi() throws Exception {
        SystemInfo si = new SystemInfo();
        CentralProcessor p = si.getHardware().getProcessor();
        HardwareAbstractionLayer hal = si.getHardware();


//        NetworkIF[] netArray = hal.getNetworkIFs();
//        for (NetworkIF net : netArray) {
//            System.out.format(" Name: %s (%s)%n", net.getName(), net.getDisplayName());
//            System.out.format("   MAC Address: %s %n", net.getMacaddr());
//            System.out.format("   MTU: %s, Speed: %s %n", net.getMTU(), FormatUtil.formatValue(net.getSpeed(), "bps"));
//            System.out.format("   IPv4: %s %n", Arrays.toString(net.getIPv4addr()));
//            System.out.format("   IPv6: %s %n", Arrays.toString(net.getIPv6addr()));
//            boolean hasData = net.getBytesRecv() > 0 || net.getBytesSent() > 0 || net.getPacketsRecv() > 0
//                    || net.getPacketsSent() > 0;
//            System.out.format("   Traffic: received %s/%s; transmitted %s/%s %n",
//                    hasData ? net.getPacketsRecv() + " packets" : "?",
//                    hasData ? FormatUtil.formatBytes(net.getBytesRecv()) : "?",
//                    hasData ? net.getPacketsSent() + " packets" : "?",
//                    hasData ? FormatUtil.formatBytes(net.getBytesSent()) : "?");
//        }

        System.out.println(p.getSystemCpuLoad());
        System.out.println(p.getSystemCpuLoadBetweenTicks());
        System.out.println(p.getSystemLoadAverage());
        System.out.println(p.getProcessorCpuLoadBetweenTicks()[0]);
        System.out.println(p.getProcessorCpuLoadBetweenTicks()[1]);

        for (int cpu = 0; cpu < p.getLogicalProcessorCount(); cpu++) {
            long[][] processorCpuLoadTicks = p.getProcessorCpuLoadTicks();
            for (int i = 0; i < processorCpuLoadTicks[cpu].length; i++) {
                System.out.println(processorCpuLoadTicks[cpu][i]);
            }
        }


        StringBuilder procCpu = new StringBuilder("CPU load per processor:");
        double[] load = hal.getProcessor().getProcessorCpuLoadBetweenTicks();
        for (int cpu = 0; cpu < load.length; cpu++) {
            procCpu.append(String.format(" %.1f%%", load[cpu] * 100));
        }
        System.out.println(procCpu.toString());

    }
}
