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

package ro.cosu.vampires.server.actors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.CounterSnapshot;
import ro.cosu.vampires.server.workload.HistogramSnapshot;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.MeterSnapshot;
import ro.cosu.vampires.server.workload.Stats;

public class StatsProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StatsProcessor.class);

    private MetricRegistry metricRegistry = new MetricRegistry();

    private Map<String, ClientInfo> clientsInfo = Maps.newHashMap();
    private Map<String, ResourceInfo> resourcesInfo = Maps.newHashMap();

    private Stats latestStats = Stats.empty();

    public void flush() {
        Map<String, HistogramSnapshot> histograms = Maps.newHashMap();
        Map<String, MeterSnapshot> meters = Maps.newHashMap();
        Map<String, CounterSnapshot> counters = Maps.newHashMap();



        metricRegistry.getHistograms().entrySet().stream().forEach(m -> {
            String name = m.getKey();
            Histogram value = m.getValue();
            histograms.put(name, HistogramSnapshot.fromHistogram(name, value));
        });

        metricRegistry.getMeters().entrySet().stream().forEach(m -> {
            String name = m.getKey();
            Meter value = m.getValue();
            meters.put(name, MeterSnapshot.fromMeter(name, value));
        });

        metricRegistry.getCounters().entrySet().stream().forEach(m -> {
            String name = m.getKey();
            Counter value = m.getValue();
            counters.put(name, CounterSnapshot.fromCounter(name, value));
        });

        latestStats = Stats.builder()
                .counters(ImmutableMap.copyOf(counters))
                .meters(ImmutableMap.copyOf(meters))
                .histograms(ImmutableMap.copyOf(histograms)).build();
    }

    public Stats getStats() {

        return latestStats;
    }

    public void process(ClientInfo message) {
        clientsInfo.put(message.id(), message);

    }

    public void process(ResourceInfo message) {
        resourcesInfo.put(message.parameters().id(), message);
    }

    public void process(Job job) {

        String from = job.from();
        // do not process jobs from strangers
        if (!resourcesInfo.containsKey(from)){
            LOG.warn("client {} not registered. skipping processing", from);
            return;
        }
        String instanceType = resourcesInfo.get(from).parameters().instanceType();
        Resource.ProviderType providerType = resourcesInfo.get(from).parameters().providerType();

        // TODO: compute cost here

        job.hostMetrics().metrics().stream().flatMap(m -> m.values().entrySet().stream())
                .forEach(e -> {
                    Double value = e.getValue();
                    if (value < 100) {
                        value = 1000. * value;
                    }
                    long rounded = Math.round(value);
                    String key = e.getKey();
                    updateMetric(providerType.name(), instanceType, from, key, rounded);
                });

        updateMetric(providerType.name(), instanceType, from, "duration", job.result().duration());
        updateMetric(providerType.name(), instanceType, from, "job", 0);

    }

    private void updateMetric(String providerType, String instanceType, String clientId, String metric, long value) {

        List<String> keys = Lists.newArrayList(metric,
                metric + ":" + providerType,
                metric + ":" + providerType + ":" + instanceType,
                metric + ":" + providerType + ":" + instanceType + ":" + clientId);

        for (String key : keys) {
            if (metric.equals("job")) {
                metricRegistry.meter(key).mark();
            } else if (metric.contains("bytes")) {
                metricRegistry.counter(key).inc(value);
            } else {
                metricRegistry.histogram(key).update(value);
            }
        }

    }
}
