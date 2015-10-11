package ro.cosu.vampires.resources;

import akka.actor.Extension;
import com.typesafe.config.Config;

public class SettingsImpl implements Extension {

    public final Config vampires;


    public SettingsImpl(Config config) {
        vampires = config.getConfig("vampires");
    }

}

