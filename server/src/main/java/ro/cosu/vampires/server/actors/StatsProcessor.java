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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.metrics.CounterSnapshot;
import ro.cosu.vampires.server.values.jobs.metrics.HistogramSnapshot;
import ro.cosu.vampires.server.values.jobs.metrics.MeterSnapshot;
import ro.cosu.vampires.server.values.jobs.metrics.Stats;
import ro.cosu.vampires.server.values.jobs.metrics.ValueSnapshot;

public class StatsProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StatsProcessor.class);

    private MetricRegistry metricRegistry = new MetricRegistry();

    private Map<String, LocalDateTime> clientsCreatedAt = Maps.newHashMap();
    private Map<String, ClientInfo> clientsInfo = Maps.newHashMap();
    private Map<String, ResourceInfo> resourcesInfo = Maps.newHashMap();

    private Stats latestStats = Stats.empty();

    public void flush() {
        Map<String, HistogramSnapshot> histograms = Maps.newHashMap();
        Map<String, MeterSnapshot> meters = Maps.newHashMap();
        Map<String, CounterSnapshot> counters = Maps.newHashMap();
        Map<String, ValueSnapshot> values = Maps.newHashMap();


        metricRegistry.getHistograms().entrySet().forEach(m -> {
            String name = m.getKey();
            Histogram value = m.getValue();
            histograms.put(name, HistogramSnapshot.fromHistogram(name, value));
        });

        metricRegistry.getMeters().entrySet().forEach(m -> {
            String name = m.getKey();
            Meter value = m.getValue();
            meters.put(name, MeterSnapshot.fromMeter(name, value));
        });

        metricRegistry.getCounters().entrySet().forEach(m -> {
            String name = m.getKey();
            Counter value = m.getValue();
            counters.put(name, CounterSnapshot.fromCounter(name, value));
        });

        metricRegistry.getGauges().entrySet().forEach(m -> {
            String name = m.getKey();
            Gauge value = m.getValue();
            values.put(name, ValueSnapshot.fromGauge(name, value));
        });

        latestStats = Stats.builder()
                .counters(ImmutableMap.copyOf(counters))
                .values(ImmutableMap.copyOf(values))
                .meters(ImmutableMap.copyOf(meters))
                .resources(ImmutableList.copyOf(resourcesInfo.values()))
                .histograms(ImmutableMap.copyOf(histograms)).build();
    }

    public Stats getStats() {
        return latestStats;
    }

    public void process(ClientInfo message) {
        String from = message.id();
        clientsInfo.put(from, message);
    }

    public void process(ResourceInfo message) {
        String from = message.parameters().id();

        clientsCreatedAt.put(from, message.createdAt());
        boolean seenBefore = resourcesInfo.containsKey(from);
        resourcesInfo.put(from, message);
        if (seenBefore) return;

        // node count
        for (String key : getKeysForMetric(from, "resources")) {
            metricRegistry.counter(key).inc();
        }

        List<String> costMetricKeys = getKeysForMetric(from, "cost");
        // remove the client key from the keys. this will provide the actual value

        String clientCostMetricKey = costMetricKeys.get(0);

        // register a cost gauge for this client
        metricRegistry.register(clientCostMetricKey, (Gauge<Double>) () -> getCostForClient(from));

        // cost gauges for the rest

        for (String key : costMetricKeys) {
            // key not registered yet
            if (!metricRegistry.getGauges().containsKey(key)) {

                metricRegistry.register(key, (Gauge<Double>) () ->
                        // get all the sibling gauges (but not the current gauge - nudge it with a :)
                        metricRegistry.getGauges((name, metric) -> name.startsWith(key + ":") &&
                                name.split(":").length - key.split(":").length == 1)
                                .values().stream()
                                .mapToDouble(g -> (Double) g.getValue())
                                .sum());
            }
        }
    }

    public void process(Job job) {

        String from = job.from();
        // do not process jobs from strangers
        if (!resourcesInfo.containsKey(from)) {
            LOG.warn("client {} not registered. skipping processing", from);
            return;
        }

        job.hostMetrics().metrics().stream().flatMap(m -> m.values().entrySet().stream())
                .forEach(e -> {
                    // converts doubles to longs
                    Double value = e.getValue();
                    if (value < 100) {
                        value = 1000. * value;
                    }
                    long rounded = Math.round(value);
                    String key = e.getKey();
                    updateMetric(from, key, rounded);
                });

        updateMetric(from, "duration", job.result().duration());
        updateMetric(from, "job", 0);

    }

    private void updateMetric(String clientId, String metric, long value) {

        for (String key : getKeysForMetric(clientId, metric)) {
            if ("job".equals(metric)) {
                metricRegistry.meter(key).mark();
            } else if (metric.contains("bytes")) {
                metricRegistry.counter(key).inc(value);
            } else {
                metricRegistry.histogram(key).update(value);
            }
        }
    }

    private double getCostForClient(String id) {

        long billedHours = getDurationForClient(id).toHours() + 1;
        double cost = resourcesInfo.get(id).parameters().cost();
        return billedHours * cost;
    }

    private Duration getDurationForClient(String id) {
        LocalDateTime createdAt = clientsCreatedAt.get(id);
        LocalDateTime now = LocalDateTime.now();

        return Duration.between(createdAt, now);
    }

    private String getInstanceType(String from) {
        return resourcesInfo.get(from).parameters().instanceType();
    }

    private Resource.ProviderType getProviderType(String from) {
        return resourcesInfo.get(from).parameters().providerType();
    }

    private List<String> getKeysForMetric(String clientId, String metric) {

        String instanceType = getInstanceType(clientId);
        Resource.ProviderType providerType = getProviderType(clientId);

        // return the keys in reverse order: most specific (longest) first
        return Lists.reverse(Lists.newArrayList(metric,
                Joiner.on(":").join(metric, providerType),
                Joiner.on(":").join(metric, providerType, instanceType),
                Joiner.on(":").join(metric, providerType, instanceType, clientId)));
    }
}
