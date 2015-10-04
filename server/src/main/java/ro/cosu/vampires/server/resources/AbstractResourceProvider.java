package ro.cosu.vampires.server.resources;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;

public abstract class AbstractResourceProvider implements ResourceProvider {

    private Config config;

    @Inject
    public void setConfig(@Named("Config") Config config) {
        this.config= config;
    }

    public Config getConfig() {
        return config;
    }
}
