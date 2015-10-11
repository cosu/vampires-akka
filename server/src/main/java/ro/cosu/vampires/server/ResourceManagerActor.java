package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import ro.cosu.vampires.resources.Settings;
import ro.cosu.vampires.resources.SettingsImpl;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceModule;
import ro.cosu.vampires.server.resources.ResourceProvider;

import java.util.LinkedList;
import java.util.List;

public class ResourceManagerActor extends UntypedActor {
    final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    ResourceManager rm;
    private List<ActorRef> resources = new LinkedList<>();

    public ResourceManagerActor(){
        Injector injector = Guice.createInjector(new ResourceModule(settings.vampires));

        rm = injector.getInstance(ResourceManager.class);

    }

    private void createResource(ResourceProvider rp, Object message) {
        ActorRef resource = getContext().actorOf(ResourceActor.props(rp));
        resource.forward(message, getContext());
        resources.add(resource);
        getContext().watch(resource);
    }

    @Override
    public void preStart() {

    }


    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Message.CreateResource) {

            rm.getProvider(((Message.CreateResource) message)
                    .type)
                    .ifPresent(rp -> createResource(rp, message));
        } else if (message instanceof Message.GetResourceInfo || message instanceof Message.DestroyResource) {
            //broadcast for now
            resources.forEach(r -> r.forward(message, getContext()));
        } else  {
            unhandled(message);
        }
    }

    public static Props props(){
        return Props.create(ResourceManagerActor.class);
    }

}
