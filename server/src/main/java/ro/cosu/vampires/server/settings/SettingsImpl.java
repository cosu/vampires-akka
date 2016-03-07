package ro.cosu.vampires.server.settings;

import akka.actor.Extension;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.JobUtil;
import ro.cosu.vampires.server.writers.ResultsWriter;
import ro.cosu.vampires.server.writers.json.JsonResultsWriter;
import ro.cosu.vampires.server.writers.mongo.MongoWriter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsImpl implements Extension {

    private static final int DEFAULT_CPU_SET_SIZE = 1;
    public final Config vampires;
    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);

    private final static int DEFAULT_MAX_JOB_DEADLINE = 60;
    private final static int DEFAULT_BACK_OFF_INTERVAL = 20;
    private final static String DEFAULT_EXECUTOR = "FORK";

    public SettingsImpl(Config config) {
        vampires = config.getConfig("vampires");
    }

    public List<ResultsWriter> getWriters() {
        List<ResultsWriter> writers = new LinkedList<>();
        List<String> enabledWriters = vampires.getStringList("enabled-writers");
        if (enabledWriters.contains("json")) {
            writers.add(new JsonResultsWriter(vampires));
        }

        if (enabledWriters.contains("mongo")) {
            writers.add(new MongoWriter());
        }

        if (writers.isEmpty()) {
            LOG.info("no writers configured. using default writer: json");
            writers.add(new JsonResultsWriter(vampires));
        }

        return writers;

    }

    public List<Job> getWorkload() {
        return JobUtil.fromConfig(vampires.getConfig("workload"));
    }

    public List<String> getExecutors() {
        if (vampires.hasPath("executors")) {
            return vampires.getStringList("executors").stream().map(String::toUpperCase).collect(Collectors.toList());
        } else {
            LOG.warn("Missing executors config value. using default {}", DEFAULT_EXECUTOR);
            return Collections.singletonList(DEFAULT_EXECUTOR);
        }

    }

    public int getBackoffInterval() {
        if (vampires.hasPath("backoffInterval")){
            return vampires.getInt("backoffInterval");
        }
        else {
            LOG.warn("missing backoffInterval. Using default value: {}", DEFAULT_BACK_OFF_INTERVAL);
        }
        return DEFAULT_BACK_OFF_INTERVAL;

    }

    public int getCpuSetSize() {
        if (vampires.hasPath("cpuSetSize")) {
            return vampires.getInt("cpuSetSize");
        } else {
            LOG.warn("missing executor cpuSetSize. Using default value: {}", DEFAULT_CPU_SET_SIZE);
        }
        return DEFAULT_CPU_SET_SIZE;
    }

    public int getJobDeadline() {

        int maxJobSeconds = DEFAULT_MAX_JOB_DEADLINE;
        if (vampires.hasPath("jobDeadlineSeconds")){
            maxJobSeconds = vampires.getInt("jobDeadlineSeconds");
        } else {
            LOG.warn("maxJobSeconds not provided. Using default value of {}", DEFAULT_MAX_JOB_DEADLINE);
        }
        return maxJobSeconds;
    }
}

