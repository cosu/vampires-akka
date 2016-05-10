package ro.cosu.vampires.server.rest.controllers;


public abstract class AbstractController implements Controller {
    AbstractController() {
        loadRoutes();
    }

}
