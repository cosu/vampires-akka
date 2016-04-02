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
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder();

        gauges.entrySet().stream().forEach(entry -> {
            String gaugeName = entry.getKey();
            String gaugeValue = entry.getValue().getValue().toString();
            builder.put(gaugeName.replace(".", "-"), gaugeValue);

        });

        return builder.build();
    }


}
