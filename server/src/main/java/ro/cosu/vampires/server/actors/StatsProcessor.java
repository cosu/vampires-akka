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

package ro.cosu.vampires.server.actors;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.HistogramSnapshot;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.Stats;

import java.util.Map;

public class StatsProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StatsProcessor.class);

    private MetricRegistry metricRegistry = new MetricRegistry();

    private Map<String, ClientInfo> clientsInfo = Maps.newHashMap();
    private Map<String, ResourceInfo> resourcesInfo = Maps.newHashMap();

    private Stats latestStats = Stats.empty();

    public void flush() {
        Map<String, HistogramSnapshot> stats = Maps.newHashMap();
        metricRegistry.getHistograms().entrySet().stream().forEach(m -> {
            String name = m.getKey();
            Histogram value = m.getValue();
            stats.put(name, HistogramSnapshot.fromHistogram(name, value));
        });
        latestStats = Stats.builder().stats(ImmutableMap.copyOf(stats)).build();
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

    }

    private void updateMetric(String providerType, String instanceType, String clientId, String metric, long value) {
        if (metric.contains("bytes")) {
            metricRegistry.counter(metric).inc(value);
            // todo
        } else {
            metricRegistry.histogram(metric).update(value);
            metricRegistry.histogram(metric + ":" + providerType).update(value);
            metricRegistry.histogram(metric + ":" + providerType + ":" + instanceType).update(value);
            metricRegistry.histogram(metric + ":" + providerType + ":" + instanceType + ":" + clientId).update(value);

        }
    }
}
