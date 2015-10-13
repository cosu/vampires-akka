package ro.cosu.vampires.server.settings;

import akka.actor.Extension;
import com.typesafe.config.Config;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SettingsImpl implements Extension {

    public final Config vampires;


    public SettingsImpl(Config config) {
        vampires = config.getConfig("vampires");
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

