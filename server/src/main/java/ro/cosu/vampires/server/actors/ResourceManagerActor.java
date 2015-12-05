package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import ro.cosu.vampires.server.resources.*;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.ClientInfo;

import java.util.*;
import java.util.stream.IntStream;

public class ResourceManagerActor extends UntypedActor {
    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ResourceManager rm;
    protected List<ActorRef> resources = new LinkedList<>();
    protected BiMap<ActorRef, String> resourceActorsToClientIds = HashBiMap.create();
    protected BiMap<String, ResourceDescription> clientIdsToDescriptions = HashBiMap.create();
    protected BiMap<ClientInfo, ActorRef> registeredClients = HashBiMap.create();


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
            resource.forward(create, getContext());
            resources.add(resource);
            getContext().watch(resource);
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
            lookupAndSendToResource(message);

        } else if (message instanceof ResourceControl.Shutdown) {
            resources.forEach(r -> r.forward(new ResourceControl.Shutdown(), getContext()));
        } else if (message instanceof ResourceInfo) {
            log.debug("resource info {}", message);
            //register resources
            final ResourceInfo resourceInfo = (ResourceInfo) message;
            resourceActorsToClientIds.put(getSender(), resourceInfo.description().id());
            clientIdsToDescriptions.put(resourceInfo.description().id(), resourceInfo.description());

        } else if (message instanceof ClientInfo) {
            final ClientInfo register = (ClientInfo) message;
            log.info("registered new client {} {}", register, clientIdsToDescriptions.get(register.id()));

            registeredClients.put(register, getSender());
            log.info("registered {}/{}", registeredClients.size(), resourceActorsToClientIds.size());

        } else if (message instanceof Terminated) {
            log.info("terminated {}", getSender());
            resources.remove(getSender());

            final String clientId = resourceActorsToClientIds.get(getSender());
            resourceActorsToClientIds.remove(getSender());
            clientIdsToDescriptions.remove(clientId);

            final Optional<ClientInfo> first = registeredClients.keySet().stream().filter(clientInfo1 -> clientInfo1
                    .id().equals(clientId)).findFirst();
            first.ifPresent(clientInfo -> registeredClients.remove(clientInfo));

            //terminate condition
            if (resources.isEmpty())
                getContext().stop(getSelf());

        } else {
            log.debug("unhandled {}", message);
            unhandled(message);
        }
    }

    private void lookupAndSendToResource(Object message) {
        final ResourceControl.Info infoRequest = (ResourceControl.Info) message;
        final ActorRef resource = resourceActorsToClientIds.inverse().get(infoRequest.resourceId);
        resource.tell(message, getSender());

    }

    public static Props props() {
        return Props.create(ResourceManagerActor.class);
    }


}
