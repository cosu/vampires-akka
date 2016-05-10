package ro.cosu.vampires.server.actors;

import com.google.inject.Guice;
import com.google.inject.Injector;

import akka.actor.Props;
import akka.actor.UntypedActor;
import ro.cosu.vampires.server.rest.controllers.ControllersModule;


public class WebserverActor extends UntypedActor {

    private ControllersModule controllersModule = new ControllersModule(getContext().system());
    private Injector injector = Guice.createInjector(controllersModule);


    public static Props props() {
        return Props.create(WebserverActor.class);
    }

    @Override
    public void onReceive(Object message) throws Exception {
    }
}
