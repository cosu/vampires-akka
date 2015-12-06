package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import ro.cosu.vampires.server.resources.*;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.ClientInfo;

import java.util.Optional;
import java.util.stream.IntStream;

public class ResourceManagerActor extends UntypedActor {
    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ResourceManager rm;

    protected ResourceRegistry resourceRegistry = new ResourceRegistry();

    public ResourceManagerActor() {
        Injector injector = Guice.createInjector(new ResourceModule(settings.vampires));

        rm = injector.getInstance(ResourceManager.class);


    }

    private void startResources() {
        settings.vampires.getConfigList("start").stream().forEach(config ->
        {
            String type = config.getString("type");
            int count = config.getInt("count");
            final Resource.Provider provider = Resource.Provider.valueOf(config.getString("provider").toUpperCase());
            log.info("starting {} x  {} from provider {}", count, type, provider);

            IntStream.rangeClosed(1, count).forEach(i ->
                    getSelf().tell(new ResourceControl.Bootstrap(provider, type), getSelf()));
        });
    }

    @Override
    public void preStart() {
        startResources();
    }

    private void createResource(ResourceControl.Create create) {
        log.info("create resource {}", create);

        final Optional<ResourceProvider> provider = rm.getProvider(create.provider);
        if (provider.isPresent()) {
            ActorRef resource = getContext().actorOf(ResourceActor.props(provider.get()));
            getContext().watch(resource);
            resourceRegistry.addResource(resource);

            resource.forward(create, getContext());


        } else {
            log.error("Error getting {} provider", create.provider);
        }
    }


    private void bootstrapResource(ResourceProvider rp, ResourceControl.Bootstrap bootstrap) {
        ResourceControl.Create create = new ResourceControl.Create(bootstrap.provider, rp.getParameters(bootstrap
                .name));

        createResource(create);
    }


    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof ResourceControl.Bootstrap) {

            ResourceControl.Bootstrap bootstrap = (ResourceControl.Bootstrap) message;
            rm.getProvider(bootstrap.provider).ifPresent(rp -> bootstrapResource(rp, bootstrap));

        } else if (message instanceof ResourceControl.Create) {
            ResourceControl.Create create = (ResourceControl.Create) message;
            createResource(create);

        } else if (message instanceof ResourceControl.Info) {
            final ResourceControl.Info info = (ResourceControl.Info) message;
            resourceRegistry.lookupResourceOfClient(info.resourceId).tell(message, getSender());

        } else if (message instanceof ResourceControl.Shutdown) {
            resourceRegistry.getResources().forEach(r -> r.forward(new ResourceControl.Shutdown(), getContext()));
        } else if (message instanceof ResourceInfo) {
            log.debug("resource info {}", message);
            //register resources
            final ResourceInfo resourceInfo = (ResourceInfo) message;
            resourceRegistry.registerResource(getSender(), resourceInfo);


        } else if (message instanceof ClientInfo) {
            final ClientInfo register = (ClientInfo) message;

            resourceRegistry.registerClient(getSender(), register);
            resourceRegistry.lookupResourceOfClient(register.id()).forward(message, getContext());

            log.info("registered {}/{}", resourceRegistry.getRegisteredClients().size(), resourceRegistry
                    .getResources().size());


        } else if (message instanceof Terminated) {
            log.info("terminated {}", getSender());
            resourceRegistry.removeResource(getSender());

            //terminate condition
            if (resourceRegistry.getResources().isEmpty())
                getContext().stop(getSelf());

        } else {
            log.debug("unhandled {}", message);
            unhandled(message);
        }
    }


    public static Props props() {
        return Props.create(ResourceManagerActor.class);
    }


}
