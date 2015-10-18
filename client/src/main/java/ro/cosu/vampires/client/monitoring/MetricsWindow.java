package ro.cosu.vampires.client.monitoring;

import autovalue.shaded.com.google.common.common.collect.ImmutableMap;
import com.codahale.metrics.Gauge;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.LocalDateTime;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

public class MetricsWindow {
    ConcurrentSkipListMap<LocalDateTime, ImmutableMap<String, Double>> metricWindow = new ConcurrentSkipListMap
            <>();

    Cache<LocalDateTime, Object> cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).removalListener(notification -> {
        if (notification.getKey() != null && notification.getKey() instanceof LocalDateTime) {
            LocalDateTime key = (LocalDateTime) notification.getKey();
            metricWindow.remove(key);
        }
    }).build();



    public void add(LocalDateTime time, SortedMap<String, Gauge> metrics) {

        metricWindow.put(time, convertGaugesToDouble(metrics));
        cache.put(time, time);
    }

    public ImmutableMap<LocalDateTime, ImmutableMap<String, Double>> getInterval(LocalDateTime start,
                                                                                 LocalDateTime stop) {


        return ImmutableMap.copyOf(metricWindow.subMap(start, stop));

    }


    public static ImmutableMap<String, Double> convertGaugesToDouble(SortedMap<String, Gauge> gauges) {
        ImmutableMap.Builder<String, Double> builder = ImmutableMap.<String, Double>builder();

        gauges.entrySet().stream().forEach(entry -> {
            String gaugeName = entry.getKey();
            Object gaugeValue  = entry.getValue().getValue();
            if (gaugeValue instanceof Number) {
                double n = ((Number) gaugeValue).doubleValue();
                if (!Double.isNaN(n) && !Double.isInfinite(n))  {
                    builder.put(gaugeName, n);
                }
            }
        });

        return builder.build();

    }

    public static ImmutableMap<String, String> convertGaugesToString(SortedMap<String, Gauge> gauges) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder();

        gauges.entrySet().stream().forEach(entry -> {
            String gaugeName = entry.getKey();
            String gaugeValue  = entry.getValue().getValue().toString();
            builder.put(gaugeName, gaugeValue);

        });

        return builder.build();

    }


}
