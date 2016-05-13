package ro.cosu.vampires.server.rest.controllers;

import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;


public class ConfigurationsController extends AbstractRestController<Configuration, ConfigurationPayload> {

    ConfigurationsController() {
        super(Configuration.class, ConfigurationPayload.class, "/configurations");
    }
}
