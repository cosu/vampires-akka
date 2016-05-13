package ro.cosu.vampires.server.rest.controllers;


import com.google.inject.AbstractModule;

import akka.actor.ActorRef;
import ro.cosu.vampires.server.rest.services.ServicesModule;

public class ControllersModule extends AbstractModule{

    private final ActorRef actorRef;

    public ControllersModule(ActorRef actorRef) {
        this.actorRef = actorRef;
    }

    @Override
    protected void configure() {
        install(new ServicesModule());
        
    }
}
