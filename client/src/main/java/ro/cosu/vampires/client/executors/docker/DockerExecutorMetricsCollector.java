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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class DockerExecutorMetricsCollector implements ExecutorMetricsCollector {
    static final Logger LOG = LoggerFactory.getLogger(DockerExecutor.class);

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
        return Metrics.builder().id(id).metadata(ImmutableMap.of()).metrics(statisticsList).build();

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

        Map<String, Double> data = new HashMap<>();
        data.putAll(convert("network", stat.getNetworkStats()));
        data.putAll(convert("memory", stat.getMemoryStats()));
        data.putAll(convert("io", stat.getBlkioStats()));
        data.putAll(convert("cpu", stat.getCpuStats()));

        return Metric.builder().values(ImmutableMap.copyOf(data)).time(LocalDateTime.now()).build();
    }


    private static Map<String, Double> convert(String prefix, Map<String, Object> stringMapMap) {
        // the reason this  method exists is the lack of a type safe way of getting metrics from
        // the docker api. it's ugly but it works (tm)
        Map<String, Double> redata = new HashMap<>();

        for (Map.Entry<String, Object> entry : stringMapMap.entrySet()) {
            String key = Joiner.on("-").join(prefix, entry.getKey().replace("_", "-"));
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
