package ro.cosu.vampires.client;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.TreeMap;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

public class TestUtil {

    protected static MetricRegistry getMetricRegistryMock() {

        MetricRegistry mock = Mockito.mock(MetricRegistry.class);

        TreeMap<String, Gauge> results = Maps.newTreeMap();
        Gauge<Double> gauge = () -> 0.;
        results.put("host", gauge);
        results.put("network", gauge);
        results.put("cpu", gauge);

        when(mock.getGauges(anyObject()))
                .thenReturn(Collections.unmodifiableSortedMap(results));

        return mock;
    }
}
