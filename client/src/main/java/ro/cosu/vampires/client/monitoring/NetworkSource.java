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

import java.time.Duration;
import java.time.LocalDateTime;

import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.platform.linux.LinuxNetworks;

import static com.codahale.metrics.MetricRegistry.name;

public class NetworkSource implements Source {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkSource.class);

    private final static String NAME = "network";


    final private MetricRegistry metricRegistry;

    private final HardwareAbstractionLayer hal;

    LinuxNetworks linuxNetworks = new LinuxNetworks();

    @Inject
    public NetworkSource(MetricRegistry metricRegistry, HardwareAbstractionLayer hal) {
        this.metricRegistry = metricRegistry;
        this.hal = hal;


    }

    public void register() {
        NetworkIF[] netArray = hal.getNetworkIFs();
        for (NetworkIF net : netArray) {
            metricRegistry.register(name(getName(), "tx-bytes", net.getName()), this.txBytesGague(net));
            metricRegistry.register(name(getName(), "rx-bytes", net.getName()), this.rxBytesGague(net));
            metricRegistry.register(name(getName(), "rx-speed", net.getName()), this.rxBytesSpeedGauge(net));
            metricRegistry.register(name(getName(), "tx-speed", net.getName()), this.txBytesSpeedGauge(net));
        }


        LOG.debug("registered network source");

    }

    @Override
    public String getName() {
        return NAME;
    }


    private Gauge<Long> txBytesGague(final NetworkIF iface) {
        return () -> {
            linuxNetworks.updateNetworkStats(iface);
            return iface.getBytesSent();
        };
    }

    private Gauge<Long> rxBytesGague(final NetworkIF iface) {

        return () -> {
            linuxNetworks.updateNetworkStats(iface);
            return iface.getBytesRecv();
        };
    }

    private Gauge<Long> txBytesSpeedGauge(final NetworkIF iface) {

        return new Gauge<Long>() {
            long previousValue = iface.getBytesRecv();
            LocalDateTime previousSample = LocalDateTime.now();

            @Override
            public Long getValue() {
                linuxNetworks.updateNetworkStats(iface);
                previousValue = iface.getBytesSent();
                Duration between = Duration.between(previousSample, LocalDateTime.now());
                double delta = iface.getBytesSent() - previousValue;
                return (delta > 0) ? Math.round(delta / between.toMillis() * 1000) : 0;

            }
        };
    }

    private Gauge<Long> rxBytesSpeedGauge(final NetworkIF iface) {

        return new Gauge<Long>() {
            long previousValue = iface.getBytesRecv();
            LocalDateTime previousSample = LocalDateTime.now();

            @Override
            public Long getValue() {
                linuxNetworks.updateNetworkStats(iface);

                Duration between = Duration.between(previousSample, LocalDateTime.now());
                double delta = iface.getBytesRecv() - previousValue;
                previousValue = iface.getBytesRecv();
                return (delta > 0) ? Math.round(delta / between.toMillis() * 1000) : 0;

            }
        };
    }


}
