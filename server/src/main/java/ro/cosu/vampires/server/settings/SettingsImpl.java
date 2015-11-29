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

    public final Config vampires;
    static final Logger LOG = LoggerFactory.getLogger(Settings.class);


    public SettingsImpl(Config config) {
        vampires = config.getConfig("vampires");
    }

    public List<ResultsWriter> getWriters() {
        List<ResultsWriter> writers = new LinkedList<>();
        List<String> enabledWriters = vampires.getStringList("enabled-writers");
        if (enabledWriters.contains("json")){
            writers.add(new JsonResultsWriter(vampires));
        }

        if (enabledWriters.contains("mongo")){
            writers.add(new MongoWriter());
        }

        if (writers.isEmpty()){
            LOG.info("no writers configured. using default writer: json");
            writers.add(new JsonResultsWriter(vampires));

        }

        return writers;

    }

    public List<String> getWorkload(){
        Config config = vampires.getConfig("workload");
        String task = config.getString("task");
        int startCount = config.getInt("start");
        int stopCount = config.getInt("stop");


        return IntStream.rangeClosed(startCount, stopCount).mapToObj(i -> String.format(task, i))
                .collect(Collectors.toList());


    }

}

