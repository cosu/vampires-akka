package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActorWithStash;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.workload.ClientInfo;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class ResourceActor extends UntypedActorWithStash {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    private final ResourceProvider resourceProvider;
    private Resource resource;

    public static Props props(ResourceProvider resourceProvider) {
        return Props.create(ResourceActor.class, resourceProvider);
    }

    ResourceActor(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }


    @Override
    public void onReceive(Object message) throws Exception {
        ActorRef sender = getSender();
        if (message instanceof ResourceControl.Create) {
            ResourceControl.Create create = (ResourceControl.Create) message;
            createResource(create, sender);
        } else if (message instanceof ResourceControl.Query) {
            sendResourceInfo(sender);
        } else if (message instanceof Resource.Status) {
            if (Resource.Status.RUNNING.equals(message)) {
                activate();
            } else {
                fail();
            }
        }
        else {
            log.debug("stash {}", message);
            stash();
        }
    }

    private void createResource(ResourceControl.Create create, ActorRef sender) {
        if (resource != null && !resource.status().equals(Resource.Status.SLEEPING)) {
            log.warning("Attempting to start an already started resource. doing nothing");
            return;
        }

        Optional<Resource> resourceOptional = resourceProvider.create(create.parameters);

        if (resourceOptional.isPresent()) {
            this.resource = resourceOptional.get();
            // do it async because activate needs a context
            // which is not available after the future completes
            this.resource.start()
                    .thenAccept(started -> {
                        log.info("started");
                        getSelf().tell(started.status(), sender);
                    }).exceptionally(exception -> signalFailed(exception, sender));
        } else {
           sendFailed(sender);
        }
    }

    private void sendFailed(ActorRef sender) {
        getSelf().tell(Resource.Status.FAILED, sender);
    }

    private Void signalFailed(Throwable throwable, ActorRef sender) {
        sendFailed(sender);
        log.error(throwable,"Actor failed to start resource");
        return null;
    }

    private Void fail() {
        log.error("actor failed to interact with resource ");
        sendInfo();
        getContext().stop(getSelf());
        return null;
    }

    private void sendInfo(){
        sendResourceInfo(getContext().parent());
        sendResourceInfo(getSender());
    }

    private void activate() {
        sendInfo();
        unstashAll();
        getContext().become(active);
    }

    @Override
    public void postStop() {
        if (resource != null)
            try {
                resource.stop().get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e, "failed to stop resource");
            }
    }

    Procedure<Object> active = message -> {
        ActorRef sender = getSender();

        if (message instanceof ResourceControl.Query) {
            sendResourceInfo(sender);
        }
        else if  (message instanceof ClientInfo) {
            connectClient((ClientInfo) message);
        }
        else if (message instanceof ResourceControl.Shutdown) {
            log.debug("shutdown " + message);
            Resource stoppedResource = resource.stop().get();
            sender.tell(stoppedResource.info(), getSelf());
        } else {
            log.error("unhandled {}", message);
            unhandled(message);
        }
    };

    private void connectClient(ClientInfo message) {
        ClientInfo clientInfo = message;
        if (clientInfo.id().equals(resource.description().id())){
            log.info("client {} connected to resource {}" ,clientInfo, resource.info());
            resource.connected();
        }
        else {
            log.error("client info and resource info don't match {}, {}", clientInfo, resource.info());
        }
    }

    private void sendResourceInfo(ActorRef toActor) {
        ResourceInfo info = Optional.ofNullable(this.resource)
                    .map(resource -> resource.info())
                    .orElse(ResourceInfo.failed(resourceProvider.getType()));
        toActor.tell(info, getSelf());
    }
}
