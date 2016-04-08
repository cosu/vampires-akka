package ro.cosu.vampires.client.executors.docker;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

import com.github.dockerjava.api.DockerClient;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import autovalue.shaded.com.google.common.common.collect.Maps;
import ro.cosu.vampires.client.executors.ExecutorMetricsCollector;
import ro.cosu.vampires.server.workload.Metrics;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;


public class DockerExecutorMetricsCollectorTest {
    @Test
    public void testExecutor() throws Exception {
        Injector injector = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ExecutorMetricsCollector.class).to(DockerExecutorMetricsCollector.class);
                    }

                    @Provides
                    private DockerClient provideDockerClient() throws InterruptedException {
                        return Mockito.mock(DockerClient.class, RETURNS_DEEP_STUBS);
                    }

                }
        );

        ExecutorMetricsCollector executorMetricsCollector = injector.getInstance(ExecutorMetricsCollector.class);

        executorMetricsCollector.startMonitoring("foo");
        executorMetricsCollector.stopMonitoring();
        Metrics metrics = executorMetricsCollector.getMetrics();

        assertThat(metrics.metadata().containsKey("docker"), is(true));
    }

    @Test
    public void testFlatenmap() {
        List<Integer> integers = Arrays.asList(1, 2, 3);
        HashMap<String, Object> map = Maps.newHashMap();
        HashMap<String, Object> subMap = Maps.newHashMap();
        map.put("list", integers);
        subMap.put("map1", integers);
        subMap.put("map2", integers);
        subMap.put("map3", integers);
        map.put("subMap", subMap);
        Map<String, Double> convertedMap = DockerExecutorMetricsCollector.flattenMap("convertedMap", map);

        assertThat(convertedMap.size(), is(12));
    }

    @Test
    public void testFlattenMapWithStrings() {
        List<String> strings = Arrays.asList("a", "b", "c");
        HashMap<String, Object> objectObjectHashMap = Maps.newHashMap();
        objectObjectHashMap.put("a", strings);
        Map<String, Double> stringDoubleMap = DockerExecutorMetricsCollector.flattenMap("f", objectObjectHashMap);
        System.out.println(stringDoubleMap);
        assertThat(stringDoubleMap.size(), is(3));

        assertThat(stringDoubleMap.values().toArray(), equalTo(new Double[]{0., 0., 0.}));
    }


}