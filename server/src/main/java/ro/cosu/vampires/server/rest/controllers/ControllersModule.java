package ro.cosu.vampires.server.rest.controllers;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import akka.actor.ActorRef;
import ro.cosu.vampires.server.rest.services.ServicesModule;

public class ControllersModule extends AbstractModule{

    private final ActorRef actorRef;

    public ControllersModule(ActorRef actorRef) {
        this.actorRef = actorRef;
    }


    @Provides
    public ActorRef getActorRef() {
        return actorRef;
    }


    @Override
    protected void configure() {
        install(new ServicesModule());
        
    }
}
