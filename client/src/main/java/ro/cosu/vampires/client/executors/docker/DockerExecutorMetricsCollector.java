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

package ro.cosu.vampires.client.executors.docker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.async.ResultCallbackTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.ws.rs.ProcessingException;

import ro.cosu.vampires.client.executors.ExecutorMetricsCollector;
import ro.cosu.vampires.server.values.jobs.metrics.Metric;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;

public class DockerExecutorMetricsCollector implements ExecutorMetricsCollector {
    private static final Logger LOG = LoggerFactory.getLogger(DockerExecutorMetricsCollector.class);

    private StatsCallback statsCallback = new StatsCallback();

    private String id;

    @Inject
    private DockerClient dockerClient;

    protected static Metric convertDockerStatsToMetrics(Statistics stat) {
        // the current stat api makes the 'read' field private so we put our own timestamp
        // future versions of the api will hopefully fix this
        Map<String, Double> data = new HashMap<>();

        Optional.ofNullable(stat.getNetworks())
                .ifPresent(stats -> data.putAll(flattenMap("network", stats)));
        data.putAll(flattenMap("memory", stat.getMemoryStats()));
//        data.putAll(flattenMap("io", stat.getBlkioStats()));
        data.putAll(flattenMap("cpu", stat.getCpuStats()));

        return Metric.builder().values(ImmutableMap.copyOf(data)).time(ZonedDateTime.now(ZoneOffset.UTC)).build();
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    protected static Map<String, Double> flattenMap(String prefix, Map<String, Object> stringMapMap) {
        // the reason this  method exists is the lack of a provider safe way of getting nested metrics from
        // the docker api. it concatenates the keys and converts all the values to doubles
        // also if it encounters any list, it flattens it by appending the index of the value in the list to the key
        // effectively a: [1,2,3] becomes a-0: 1, a-1:2, a-2:3
        // it's ugly but it works (tm)
        Map<String, Double> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : stringMapMap.entrySet()) {
            String key = Joiner.on("-").join(prefix, entry.getKey().replace("_", "-"));
            Object val = entry.getValue();

            if (val instanceof Map) {
                result.putAll(flattenMap(key, (Map<String, Object>) val));
            } else if (val instanceof List) {
                final List valAsList = (List) val;

                IntStream.range(0, valAsList.size())
                        .filter(i -> valAsList.size() >= i)
                        .forEach(i -> Optional.ofNullable(getDoubleFrom(valAsList.get(i)))
                                .ifPresent(newValue -> result.put(key + "-" + i, newValue)));
            } else {
                Optional.ofNullable(getDoubleFrom(val)).ifPresent(newValue -> result.put(key,
                        newValue));
            }
        }
        return result;
    }

    private static Double getDoubleFrom(Object object) {
        Double val = 0.;
        try {

            val = Double.parseDouble(object.toString());
        } catch (Exception e) {
            LOG.warn("can't convert {} to double", object);
        }
        return val;

    }

    @Override
    public void startMonitoring(String id) {
        this.id = id;
        dockerClient.statsCmd(id).exec(statsCallback);
    }

    @Override
    public void stopMonitoring() {
        try {
            statsCallback.close();
        } catch (IOException | ProcessingException e) {
            LOG.error("failed to close collector ", e);
        }
    }

    @Override
    public Metrics getMetrics() {
        final ImmutableList<Metric> statisticsList = ImmutableList.copyOf(statsCallback.getStatisticsList());
        return Metrics.builder().id(id).metadata(ImmutableMap.of("docker", id)).metrics(statisticsList).build();
    }

    private static class StatsCallback extends ResultCallbackTemplate<StatsCallback, Statistics> {
        private List<Metric> statisticsList = new LinkedList<>();

        @Override
        public void onNext(Statistics stats) {
            statisticsList.add(convertDockerStatsToMetrics(stats));
        }

        public List<Metric> getStatisticsList() {
            return statisticsList;
        }
    }
}
