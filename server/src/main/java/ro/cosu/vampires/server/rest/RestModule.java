package ro.cosu.vampires.server.rest;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.util.List;

import akka.actor.ActorRef;
import ro.cosu.vampires.server.rest.controllers.ControllersModule;
import ro.cosu.vampires.server.rest.services.ServicesModule;


public class RestModule extends AbstractModule {
    private final ActorRef actorRef;
    private List<String> providers;

    public RestModule(ActorRef actorRef, List<String> providers) {
        this.actorRef = actorRef;
        this.providers = providers;
    }

    @Override
    protected void configure() {
        install(new ServicesModule());
        install(new ControllersModule());
    }

    @Provides
    public List<String> getProviders() {
        return providers;
    }

    @Provides
    public ActorRef getActorRef() {
        return actorRef;
    }

}
