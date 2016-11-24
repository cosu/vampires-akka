package ro.cosu.vampires.server.rest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.cosu.vampires.server.rest.JsonTransformer;
import spark.Route;
import spark.Spark;

public class AuthController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private static String path = "/login";
    AuthController() {
        Spark.post(path, login(), JsonTransformer.get());
    }

    private Route login() {
        return (request, response) -> {
            LOG.debug("user {} authenticated", request.session().attribute("user").toString());
            return "OK";
        };
    }
}
