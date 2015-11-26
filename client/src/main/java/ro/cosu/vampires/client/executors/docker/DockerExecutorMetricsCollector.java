package ro.cosu.vampires.client.executors.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.client.executors.ExecutorMetricsCollector;
import ro.cosu.vampires.server.workload.Metric;
import ro.cosu.vampires.server.workload.Metrics;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

public class DockerExecutorMetricsCollector implements ExecutorMetricsCollector {
    static final Logger LOG = LoggerFactory.getLogger(DockerExecutorMetricsCollector.class);

    StatsCallback statsCallback = new StatsCallback();

    String id;

    @Inject
    DockerClient dockerClient;

    @Override
    public void startMonitoring(String id) {
        this.id = id;
        dockerClient.statsCmd().withContainerId(id).exec(statsCallback);

    }

    @Override
    public void stopMonitoring() {
        try {
            statsCallback.close();
        } catch (IOException e) {
            LOG.error("failed to close collector ", e);
        }
    }

    @Override
    public Metrics getMetrics() {
        final ImmutableList<Metric> statisticsList = ImmutableList.copyOf(statsCallback.getStatisticsList());
        return Metrics.builder().id(id).metadata(ImmutableMap.of("docker", id)).metrics(statisticsList).build();

    }

    private class StatsCallback extends ResultCallbackTemplate<StatsCallback, Statistics> {


        List<Metric> statisticsList = new LinkedList<>();

        @Override
        public void onNext(Statistics stats) {
            // NOTE: future versions of the docker api will break this

            statisticsList.add(convertDockerStatsToMetrics(stats));
        }

        public List<Metric> getStatisticsList() {
            return statisticsList;
        }
    }

    private static Metric convertDockerStatsToMetrics(Statistics stat) {

        // the current stat api makes the 'read' field private so we put our own timestamp
        // future versions of the api will hopefully fix this
        Map<String, Double> data = new HashMap<>();
        data.putAll(flattenMap("network", stat.getNetworkStats()));
        data.putAll(flattenMap("memory", stat.getMemoryStats()));
        data.putAll(flattenMap("io", stat.getBlkioStats()));
        data.putAll(flattenMap("cpu", stat.getCpuStats()));

        return Metric.builder().values(ImmutableMap.copyOf(data)).time(LocalDateTime.now()).build();
    }

    @SuppressWarnings("unchecked")

    private static Map<String, Double> flattenMap(String prefix, Map<String, Object> stringMapMap) {
        // the reason this  method exists is the lack of a type safe way of getting nested metrics from
        // the docker api. it concatenates the keys and converts all the values to doubles
        // also if it encounters any list, it flattens it by appending the index of the value in the list to the key
        // effectively a: [1,2,3] becomes a-0: 1, a-1:2, a-2:3
        // it's ugly but it works (tm)
        Map<String, Double> redata = new HashMap<>();

        for (Map.Entry<String, Object> entry : stringMapMap.entrySet()) {
            String key = Joiner.on("-").join(prefix, entry.getKey().replace("_", "-"));
            Object val = entry.getValue();

            if (val instanceof Map) {
                redata.putAll(flattenMap(key, (Map<String, Object>) val));
            } else if (val instanceof List) {
                final List valAsList = (List) val;

                IntStream.range(0, valAsList.size())
                        .filter(i -> valAsList.size() >= i)
                        .forEach(i -> Optional.ofNullable(getDoubleFrom(valAsList.get(i)))
                                .ifPresent(newValue -> redata.put(key + "-" + i, newValue)));


            } else {
                Optional.ofNullable(getDoubleFrom(val)).ifPresent(newValue -> redata.put(key,
                        newValue));
            }
        }

        return redata;
    }
    private static Double getDoubleFrom(Object object){
        Double val = 0.;
        try {

            val = Double.parseDouble(object.toString());
        }
        catch (Exception e){
            LOG.warn("can't convert {} to double", object);
        }
        return  val;

    }
}
