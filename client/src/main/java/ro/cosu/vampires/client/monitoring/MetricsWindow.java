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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ro.cosu.vampires.server.workload.Metric;

import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MetricsWindow {

    private int MAX_JOB_LENGTH = 5;
    private ConcurrentSkipListMap<LocalDateTime, ImmutableMap<String, Double>> metricWindow =
            new ConcurrentSkipListMap<>();

    private Cache<LocalDateTime, Object> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(MAX_JOB_LENGTH, TimeUnit.MINUTES)
            .removalListener(notification -> {
                if (notification.getKey() != null && notification.getKey() instanceof LocalDateTime) {
                    LocalDateTime key = (LocalDateTime) notification.getKey();
                    metricWindow.remove(key);
                }
            }).build();

    private static ImmutableMap<String, Double> convertGaugesToDouble(SortedMap<String, Gauge> gauges) {
        ImmutableMap.Builder<String, Double> builder = ImmutableMap.builder();

        gauges.entrySet().stream().forEach(entry -> {
            String gaugeName = entry.getKey();
            Object gaugeValue = entry.getValue().getValue();
            if (gaugeValue instanceof Number) {
                double n = ((Number) gaugeValue).doubleValue();
                if (!Double.isNaN(n) && !Double.isInfinite(n)) {
                    //replace dots with - ;dots in field names are bad
                    builder.put(gaugeName.replace(".", "-"), n);
                }
            }
        });

        return builder.build();
    }

    public static ImmutableMap<String, String> convertGaugesToString(SortedMap<String, Gauge> gauges) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        gauges.entrySet().stream().forEach(entry -> {
            String gaugeName = entry.getKey();
            String gaugeValue = entry.getValue().getValue().toString();
            builder.put(gaugeName.replace(".", "-"), gaugeValue);

        });

        return builder.build();
    }

    public void add(LocalDateTime time, SortedMap<String, Gauge> metrics) {
        metricWindow.put(time, convertGaugesToDouble(metrics));
        cache.put(time, time);
    }

    public ImmutableList<Metric> getInterval(LocalDateTime start, LocalDateTime stop) {
        List<Metric> metricList = metricWindow.subMap(start, stop).entrySet().stream().map(
                entry -> Metric.builder()
                        .time(entry.getKey())
                        .values(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return ImmutableList.copyOf(metricList);
    }


}
