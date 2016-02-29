package ro.cosu.vampires.server.settings;

import akka.actor.Extension;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.writers.ResultsWriter;
import ro.cosu.vampires.server.writers.json.JsonResultsWriter;
import ro.cosu.vampires.server.writers.mongo.MongoWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SettingsImpl implements Extension {

    private static final int DEFAULT_CPU_SET_SIZE = 1;
    public final Config vampires;
    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);

    private final static int MAX_JOB_DEADLINE = 60;
    private int DEFAULT_BACK_OFF_INTERVAl  = 20;

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

    public List<String> getWorkload() {
        Config config = vampires.getConfig("workload");
        String task = config.getString("task");
        int startCount = config.getInt("start");
        int stopCount = config.getInt("stop");


        return IntStream.rangeClosed(startCount, stopCount).mapToObj(i -> String.format(task, i))
                .collect(Collectors.toList());

    }

    public List<String> getExecutors() {
        if (vampires.hasPath("executors")) {
            return vampires.getStringList("executors").stream().map(String::toUpperCase).collect(Collectors.toList());
        } else {
            LOG.error("missing executors config value");
            throw new IllegalArgumentException("missing executors config value");
        }

    }

    public int getBackoffInterval() {
        if (vampires.hasPath("backoffInterval")){
            return vampires.getInt("backoffInterval");
        }
        else {
            LOG.error("missing backoffInterval. Using default value: {}", DEFAULT_BACK_OFF_INTERVAl);
        }
        return DEFAULT_BACK_OFF_INTERVAl;

    }

    public int getCpuSetSize() {
        if (vampires.hasPath("cpuSetSize")) {
            return vampires.getInt("cpuSetSize");
        } else {
            LOG.error("missing executor cpuSetSize. Using default value: {}", DEFAULT_CPU_SET_SIZE);
        }
        return DEFAULT_CPU_SET_SIZE;
    }

    public int getJobDeadline() {

        int maxJobSeconds = MAX_JOB_DEADLINE;
        if (vampires.hasPath("jobDeadlineSeconds")){
            maxJobSeconds = vampires.getInt("jobDeadlineSeconds");
        } else {
            LOG.warn("maxJobSeconds not provided. Using default value of {}", MAX_JOB_DEADLINE);
        }
        return maxJobSeconds;
    }
}

