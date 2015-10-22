package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceModule;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;

import java.util.LinkedList;
import java.util.List;

public class ResourceManagerActor extends UntypedActor {
    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ResourceManager rm;
    private List<ActorRef> resources = new LinkedList<>();

    public ResourceManagerActor() {
        Injector injector = Guice.createInjector(new ResourceModule(settings.vampires));

        rm = injector.getInstance(ResourceManager.class);

    }

    private void createResource(ResourceProvider rp, ResourceControl.Create create) {
        log.info("create resource {}", create);
        ActorRef resource = getContext().actorOf(ResourceActor.props(rp));
        resource.forward(create, getContext());
        resources.add(resource);
        getContext().watch(resource);

    }

    private void startResource(ResourceProvider rp, ResourceControl.Start start) {
        ResourceControl.Create create = new ResourceControl.Create(start.type, rp.getParameters(start.name));
        createResource(rp, create);
    }


    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof ResourceControl.Start) {

            ResourceControl.Start start = (ResourceControl.Start) message;
            rm.getProvider(start.type).ifPresent(rp -> startResource(rp, start));

        }

        if (message instanceof ResourceControl.Create) {
            ResourceControl.Create create = (ResourceControl.Create) message;
            rm.getProvider(create.type).ifPresent(rp -> createResource(rp, create));

        } else if (message instanceof ResourceControl.Info) {
            //broadcast for now
            resources.forEach(r -> r.forward(message, getContext()));

        } else if (message instanceof ResourceControl.Shutdown) {
            resources.forEach(r -> r.forward(new ResourceControl.Destroy(), getContext()));

        } else if (message instanceof Terminated) {
            log.info("terminated {}", getSender());
            resources.remove(getSender());
            //terminate condition
            if (resources.isEmpty())
                getContext().stop(getSelf());

        } else {
            unhandled(message);
        }
    }

    public static Props props() {
        return Props.create(ResourceManagerActor.class);
    }


}
