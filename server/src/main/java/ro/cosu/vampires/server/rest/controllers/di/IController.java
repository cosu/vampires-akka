package ro.cosu.vampires.server.rest.controllers.di;


import spark.Route;

public interface IController {

    Route list();

    Route get();

    Route update();

    Route delete();

}
