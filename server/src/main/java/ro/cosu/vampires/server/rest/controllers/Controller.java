package ro.cosu.vampires.server.rest.controllers;


import spark.Route;

public interface Controller {

    Route list();

    Route get();

    Route update();

    Route delete();

}
