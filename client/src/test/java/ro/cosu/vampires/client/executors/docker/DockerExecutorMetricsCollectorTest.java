package ro.cosu.vampires.client.executors.docker;

import autovalue.shaded.com.google.common.common.collect.Lists;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DockerExecutorMetricsCollectorTest {
    @Test
    public void testConvertDockerStatsToMetrics() throws Exception {
        Map<String, Object> stringObjectMap= new HashMap<>();
        Map<String, Object> stringObjectMap1= new HashMap<>();
        final HashMap<String, Double> doubleHashMap = Maps.newHashMap();
        final HashMap<String, List<Double>> doubStringListHashMap= Maps.newHashMap();
        doubStringListHashMap.put("bar", Lists.newArrayList(1.,2.,3.));
        doubleHashMap.put("foo", 2.);
        stringObjectMap1.put("baz", doubStringListHashMap);

        stringObjectMap1.put("doubleHashMap" , doubleHashMap);
        stringObjectMap.put("stringObjectMap1", stringObjectMap1);


        final Map<String, Double> convert = convert("", stringObjectMap);
        System.out.println(convert);
        assertThat(convert.keySet().contains("stringObjectMap1-doubleHashMap-foo"), is(true));

    }



    private Map<String, Double>convert(String prefix, Map<String, Object> stringMapMap) {

        Map<String, Double> redata = new HashMap<>();

        for (Map.Entry<String, Object> entry : stringMapMap.entrySet()) {
            String key;
            if (!prefix.isEmpty())
                key = Joiner.on("-").join(prefix, entry.getKey());
            else
                key = entry.getKey();
            Object val = entry.getValue();

            if (val instanceof Map) {
                redata.putAll(convert(key, (Map<String, Object>) val));
            } else if (val instanceof List) {
                final List valAsList = (List) val;

                IntStream.range(0, valAsList.size())
                        .filter(i -> valAsList.size() >= i)
                        .forEach(i -> redata.put(key + "-" + i, Double.parseDouble(valAsList.get(i).toString())));

            } else {
                redata.put(key, Double.parseDouble(val.toString()));
            }
        }

        return redata;
    }


}


