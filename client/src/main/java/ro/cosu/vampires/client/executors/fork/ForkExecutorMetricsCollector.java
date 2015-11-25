package ro.cosu.vampires.client.executors.fork;

import com.google.inject.Inject;
import org.hyperic.sigar.Sigar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.client.executors.ExecutorMetricsCollector;
import ro.cosu.vampires.server.workload.Metrics;

public class ForkExecutorMetricsCollector implements ExecutorMetricsCollector{
    static final Logger LOG = LoggerFactory.getLogger(ForkExecutorMetricsCollector.class);

    @Inject
    Sigar sigar;

    @Override
    public void startMonitoring(String id) {


    }

    @Override
    public void stopMonitoring() {

    }

    @Override
    public Metrics getMetrics() {
        return null;
    }
}
