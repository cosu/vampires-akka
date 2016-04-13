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
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codahale.metrics.MetricRegistry.name;

public class CpuSource implements Source {

    private static final Logger LOG = LoggerFactory.getLogger(CpuSource.class);

    private static String NAME = "cpu";

    @Inject
    private Sigar sigar;
    @Inject
    private MetricRegistry metricRegistry;

    private static double round(double d) {
        return (((double) Math.round(d * 1000)) / 1000D);
    }

    public void register() {
        metricRegistry.register(name(getName(), "cpu-total-usage"), this.cpuTotalUsageGague());
        metricRegistry.register(name(getName(), "cpu-wait-usage"), this.cpuWaitUsageGague());
        metricRegistry.register(name(getName(), "cpu-user-usage"), this.cpuUserUsageGague());
        metricRegistry.register(name(getName(), "load-one-minute-average"), this.oneMinLoadGague());
        metricRegistry.register(name(getName(), "load-five-minute-average"), this.fiveMinLoadGague());
        metricRegistry.register(name(getName(), "load-fifteen-minute-average"), this
                .fifteenMinLoadGague());


        LOG.debug("cpu network source");
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Gauge<Double> cpuUserUsageGague() {
        return () -> {
            try {
                CpuPerc cpuPc = sigar.getCpuPerc();
                return round(cpuPc.getUser());
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<Double> cpuWaitUsageGague() {
        return () -> {
            try {
                CpuPerc cpuPc = sigar.getCpuPerc();
                return round(cpuPc.getWait());
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<Double> cpuTotalUsageGague() {
        return () -> {
            try {
                CpuPerc cpuPc = sigar.getCpuPerc();
                return round(cpuPc.getCombined());
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<Double> oneMinLoadGague() {
        return () -> {
            try {
                double[] la = sigar.getLoadAverage();
                return la[0];
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<Double> fiveMinLoadGague() {
        return () -> {
            try {

                double[] la = sigar.getLoadAverage();
                return la[1];
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<Double> fifteenMinLoadGague() {
        return () -> {
            try {
                double[] la = sigar.getLoadAverage();
                return la[2];
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

}
