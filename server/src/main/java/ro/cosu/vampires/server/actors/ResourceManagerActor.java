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

import static ro.cosu.vampires.server.actors.ResourceControl.*;
import static ro.cosu.vampires.server.actors.ResourceControl.Shutdown;

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
        if (!settings.vampires.hasPath("start")) {
            log.error("no start config found. exiting");
            getContext().actorSelection("/user/terminator").tell(new Shutdown(), getSelf());
            return;
        }
        settings.vampires.getConfigList("start").stream().forEach(config ->
        {
            String type = config.getString("type");
            int count = config.getInt("count");
            final Resource.Type provider = Resource.Type.valueOf(config.getString("provider").toUpperCase());
            log.info("starting {} x {} from type {}", count, type, provider);

            IntStream.rangeClosed(1, count).forEach(i ->
                    getSelf().tell(new Bootstrap(provider, type), getSelf()));
        });
    }

    @Override
    public void preStart() {
        getContext().actorSelection("/user/terminator").tell(new Up(), getSelf());
        startResources();
    }

    private void createResource(Create create) {
        log.debug("create resource {}", create.type);

        final Optional<ResourceProvider> provider = rm.getProvider(create.type);
        if (provider.isPresent()) {
            ActorRef resourceActor = getContext().actorOf(ResourceActor.props(provider.get()));
            getContext().watch(resourceActor);
            resourceRegistry.addResourceActor(resourceActor);
            resourceActor.forward(create, getContext());
        } else {
            log.error("Error getting {} type resource provider.", create.type);
        }
    }

    private void bootstrapResource(ResourceProvider resourceProvider, Bootstrap bootstrap) {
        Create create = new Create(
                bootstrap.type, resourceProvider.getParameters(bootstrap.name));

        createResource(create);
    }

    @Override
    public void onReceive(Object message) throws Exception {

        ActorRef sender = getSender();
        if (message instanceof Bootstrap) {
            final Bootstrap bootstrap = (Bootstrap) message;
            bootstrapResource(bootstrap);
        } else if (message instanceof Create) {
            final Create create = (Create) message;
            createResource(create);
        } else if (message instanceof Query) {
            final Query query = (Query) message;
            queryResource(query, sender);
        } else if (message instanceof Shutdown) {
            shutdownResources();
        } else if (message instanceof ResourceInfo) {
            final ResourceInfo resourceInfo = (ResourceInfo) message;
            registerResource(resourceInfo, sender);
        } else if (message instanceof ClientInfo) {
            final ClientInfo clientInfo = (ClientInfo) message;
            registerClient(clientInfo);
        } else if (message instanceof Terminated) {
            terminatedResource(sender);
        } else {
            log.debug("unhandled {}", message);
            unhandled(message);
        }
    }

    private void bootstrapResource(Bootstrap bootstrap) {
        rm.getProvider(bootstrap.type).ifPresent(rp -> bootstrapResource(rp, bootstrap));
    }

    private void terminatedResource(ActorRef sender) {
        log.debug("terminated {}", sender);
        resourceRegistry.removeResource(sender);
        if (resourceRegistry.getResourceActors().isEmpty())
            getContext().stop(getSelf());
    }

    private void registerClient(ClientInfo clientInfo) {
        getContext().watch(getSender());
        log.debug("watch {}" , getSender());
        resourceRegistry.registerClient(getSender(), clientInfo);
        resourceRegistry.lookupResourceOfClient(clientInfo.id())
                .ifPresent(resourceActor -> resourceActor.forward(clientInfo, getContext()));
        logCurrentClients();
    }

    private void logCurrentClients() {
        log.info("clients {} / resourceActors {} ", resourceRegistry.getRegisteredClients().size(), resourceRegistry
                .getResourceActors().size());
    }

    private void registerResource(ResourceInfo resourceInfo, ActorRef sender) {
        log.debug("resource register {}", resourceInfo);
        resourceRegistry.registerResource(sender, resourceInfo);
    }

    private void queryResource(Query query, ActorRef sender) {
        Optional<ActorRef> resourceOfClient = resourceRegistry.lookupResourceOfClient(query.resourceId);

        if (resourceOfClient.isPresent()) {
            ActorRef resourceActor = resourceOfClient.get();
            resourceActor.tell(query, sender);
        }
        else {
            log.warning("Query: {} does not match any existing resource" , query);
        }
    }

    private void shutdownResources() {
        resourceRegistry.getResourceActors().forEach(r -> r.forward(new Shutdown(), getContext()));
    }


    public static Props props() {
        return Props.create(ResourceManagerActor.class);
    }


}
