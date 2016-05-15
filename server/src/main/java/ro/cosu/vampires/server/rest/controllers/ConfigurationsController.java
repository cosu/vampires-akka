package ro.cosu.vampires.server.rest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;


public class ConfigurationsController extends AbstractRestController<Configuration, ConfigurationPayload> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsController.class);

    private final static String path = "/configurations";

    ConfigurationsController() {
        super(Configuration.class, ConfigurationPayload.class, path);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
