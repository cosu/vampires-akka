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

import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.codahale.metrics.MetricRegistry.name;

public class NetworkSource implements Source {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkSource.class);

    private final static String NAME = "network";

    @Inject
    private Sigar sigar;
    @Inject
    private MetricRegistry metricRegistry;

    public void register() {
        try {
            Arrays.stream(sigar.getNetInterfaceList()).forEach(iface -> {
                metricRegistry.register(name(getName(), "tx-bytes", iface), this.txBytesGague(iface));
                metricRegistry.register(name(getName(), "rx-bytes", iface), this.rxBytesGague(iface));
                metricRegistry.register(name(getName(), "rx-speed", iface), this.rxBytesSpeedGauge(iface));
                metricRegistry.register(name(getName(), "tx-speed", iface), this.txBytesSpeedGauge(iface));

            });
        } catch (SigarException e) {
            LOG.error("network metric register failed ", e);
        }

        LOG.debug("registered network source");

    }

    @Override
    public String getName() {
        return NAME;
    }


    private Gauge<Long> txBytesGague(final String iface) {
        return () -> {
            try {
                NetInterfaceStat nis = sigar.getNetInterfaceStat(iface);
                return nis.getTxBytes();
            } catch (SigarException e) {
                LOG.error("network metric register failed ", e);
            }
            return null;
        };
    }


    private Gauge<Long> txBytesSpeedGauge(final String iface) {

        return new Gauge<Long>() {
            long previousValue = 0;
            LocalDateTime previousSample = LocalDateTime.now();

            @Override
            public Long getValue() {
                try {
                    NetInterfaceStat nis = sigar.getNetInterfaceStat(iface);
                    previousValue = nis.getTxBytes();
                    Duration between = Duration.between(previousSample, LocalDateTime.now());
                    long delta = nis.getTxBytes() - previousValue;
                    return (delta / between.toMillis() > 0) ? delta / between.toMillis() : 0;
                } catch (SigarException e) {
                    LOG.error("network metric register failed ", e);
                }
                return null;
            }
        };
    }

    private Gauge<Long> rxBytesSpeedGauge(final String iface) {

        return new Gauge<Long>() {
            long previousValue = 0;
            LocalDateTime previousSample = LocalDateTime.now();

            @Override
            public Long getValue() {
                try {
                    NetInterfaceStat nis = sigar.getNetInterfaceStat(iface);
                    previousValue = nis.getRxBytes();
                    Duration between = Duration.between(previousSample, LocalDateTime.now());
                    long delta = nis.getTxBytes() - previousValue;
                    return (delta > 0) ? delta / between.toMillis() : 0;
                } catch (SigarException e) {
                    LOG.error("network metric register failed ", e);
                }
                return null;
            }
        };
    }

    private Gauge<Long> rxBytesGague(final String iface) {
        return () -> {
            try {
                NetInterfaceStat nis = sigar.getNetInterfaceStat(iface);
                return nis.getRxBytes();
            } catch (SigarException e) {
                LOG.error("network metric register failed ", e);
            }
            return null;
        };
    }
}
