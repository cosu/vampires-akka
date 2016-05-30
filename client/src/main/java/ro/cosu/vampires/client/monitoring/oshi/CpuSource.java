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

package ro.cosu.vampires.client.monitoring.oshi;

import com.google.inject.Inject;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

import oshi.hardware.CentralProcessor;
import ro.cosu.vampires.client.monitoring.NetworkSource;
import ro.cosu.vampires.client.monitoring.Source;

import static com.codahale.metrics.MetricRegistry.name;

public class CpuSource implements Source {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkSource.class);

    private final static String NAME = "cpu";

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    private CentralProcessor processor;

    @Override
    public void register() {
        metricRegistry.register(name(getName(), "average-load"), cpuSystemLoadAverage());
        metricRegistry.register(name(getName(), "load"), cpuSystemCpuLoad());

        IntStream.range(0, processor.getLogicalProcessorCount()).forEach(i -> {
            metricRegistry.register(name(getName(), "load-tick-user-" + i), cpuProcessorCpuLoadTick(i, 0));
            metricRegistry.register(name(getName(), "load-tick-nice-" + i), cpuProcessorCpuLoadTick(i, 1));
            metricRegistry.register(name(getName(), "load-tick-system-" + i), cpuProcessorCpuLoadTick(i, 2));
            metricRegistry.register(name(getName(), "load-tick-idle-" + i), cpuProcessorCpuLoadTick(i, 3));
        });
    }

    private Gauge<Double> cpuSystemLoadAverage() {
        return () -> processor.getSystemLoadAverage();
    }

    private Gauge<Double> cpuSystemCpuLoad() {
        return () -> processor.getSystemCpuLoad();
    }

    private Gauge<Long> cpuProcessorCpuLoadTick(final int cpuId, int metricId) {
        return () -> {
            long[][] processorCpuLoadTicks = processor.getProcessorCpuLoadTicks();
            return processorCpuLoadTicks[cpuId][metricId];
        };
    }


//    }
//    private Gauge<Double> cpuSystemCpuLoad() {
//        return () -> hal.getProcessor().getSystemCpuLoad();
//    }


    @Override
    public String getName() {
        return NAME;
    }
}
