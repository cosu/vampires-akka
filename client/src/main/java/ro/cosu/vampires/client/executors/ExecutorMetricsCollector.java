package ro.cosu.vampires.client.executors;

import ro.cosu.vampires.server.workload.Metrics;

public interface ExecutorMetricsCollector {
    void startMonitoring(String id);
    void stopMonitoring();
    Metrics getMetrics();

}
