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
            final Resource.Type provider = Resource.Type.valueOf(config.getString("type").toUpperCase());
            log.info("starting {} x  {} from type {}", count, type, provider);

            IntStream.rangeClosed(1, count).forEach(i ->
                    getSelf().tell(new Bootstrap(provider, type), getSelf()));
        });
    }

    @Override
    public void preStart() {
        startResources();
    }

    private void createResource(Create create) {
        log.info("create resource {}", create);

        final Optional<ResourceProvider> provider = rm.getProvider(create.type);
        if (provider.isPresent()) {
            ActorRef resource = getContext().actorOf(ResourceActor.props(provider.get()));
            getContext().watch(resource);
            resourceRegistry.addResource(resource);
            resource.forward(create, getContext());

        } else {
            log.error("Error getting {} type", create.type);
        }
    }


    private void bootstrapResource(ResourceProvider resourceProvider, Bootstrap bootstrap) {
        Create create = new Create(
                bootstrap.type, resourceProvider.getParameters(bootstrap.name));

        createResource(create);
    }


    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Bootstrap) {
            Bootstrap bootstrap = (Bootstrap) message;
            bootstrapResource(bootstrap);
        } else if (message instanceof Create) {
            Create create = (Create) message;
            createResource(create);
        } else if (message instanceof Query) {
            final Query query = (Query) message;
            queryResource(query);
        } else if (message instanceof Shutdown) {
            shutdownResources();
        } else if (message instanceof ResourceInfo) {
            final ResourceInfo resourceInfo = (ResourceInfo) message;
            registerResource(resourceInfo);
        } else if (message instanceof ClientInfo) {
            final ClientInfo clientInfo = (ClientInfo) message;
            registerClient(clientInfo);
        } else if (message instanceof Terminated) {
            terminateResource();
        } else {
            log.debug("unhandled {}", message);
            unhandled(message);
        }
    }

    private void bootstrapResource(Bootstrap bootstrap) {
        rm.getProvider(bootstrap.type).ifPresent(rp -> bootstrapResource(rp, bootstrap));
    }

    private void terminateResource() {
        log.info("terminated {}", getSender());
        resourceRegistry.removeResource(getSender());
        if (resourceRegistry.getResources().isEmpty())
            getContext().stop(getSelf());
    }

    private void registerClient(ClientInfo clientInfo) {
        resourceRegistry.registerClient(getSender(), clientInfo);
        resourceRegistry.lookupResourceOfClient(clientInfo.id()).forward(clientInfo, getContext());
        logCurrentClients();
    }

    private void logCurrentClients() {
        log.info("clients {} / resources {} ", resourceRegistry.getRegisteredClients().size(), resourceRegistry
                .getResources().size());
    }

    private void registerResource(ResourceInfo resourceInfo) {
        log.debug("resource info {}", resourceInfo);
        resourceRegistry.registerResource(getSender(), resourceInfo);
    }

    private void queryResource(Query query) {
        resourceRegistry.lookupResourceOfClient(query.resourceId).tell(query, getSender());
    }

    private void shutdownResources() {
        resourceRegistry.getResources().forEach(r -> r.forward(new Shutdown(), getContext()));
    }


    public static Props props() {
        return Props.create(ResourceManagerActor.class);
    }


}
