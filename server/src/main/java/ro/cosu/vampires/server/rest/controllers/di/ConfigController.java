package ro.cosu.vampires.server.rest.controllers.di;

import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;


public class ConfigController extends AbstractRestController<Configuration, ConfigurationPayload> {

    ConfigController() {
        super(Configuration.class, ConfigurationPayload.class);
    }
}
