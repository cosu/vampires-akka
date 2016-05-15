package ro.cosu.vampires.server.rest.controllers;


import com.google.inject.Inject;

import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.rest.services.ProvidersService;
import spark.Route;
import spark.Spark;

public class ProvidersController {

    @Inject
    ProvidersService service;

    ProvidersController() {
        Spark.get("/providers", list(), JsonTransformer.get());
    }

    public Route list() {
        return (request, response) -> service.list();
    }


}
