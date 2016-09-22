package ro.cosu.vampires.server.actors.resource;


import java.util.concurrent.ExecutionException;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.QueryResource;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.values.ClientInfo;

public class ResourceActor extends UntypedActor {

    private final Resource resource;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    ResourceActor(Resource resource) {
        this.resource = resource;
    }

    public static Props props(Resource resource) {
        return Props.create(ResourceActor.class, resource);
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        ActorRef sender = getSender();
        if (message instanceof QueryResource) {
            sendResourceInfo(sender);
        } else if (message instanceof ResourceInfo) {
            handleResourceInfo((ResourceInfo) message);
        } else if (message instanceof ClientInfo) {
            connectClient((ClientInfo) message);
        } else if (message instanceof ResourceControl.Shutdown) {
            stop();
        } else if (message instanceof ResourceControl.Start) {
            start();
        } else {
            log.error("unhandled {}", message);
            unhandled(message);
        }
    }

    private void handleResourceInfo(ResourceInfo resourceInfo) {
        sendResourceInfo(getContext().parent());
        if (!Resource.Status.RUNNING.equals(resourceInfo.status())) {
            log.error("actor failed to interact with resource ");
            getContext().stop(getSelf());
        }
    }

    private void stop() throws ExecutionException, InterruptedException {
        ActorRef sender = getSender();
        resource.stop()
                .thenAccept(resource ->
                {
                    sendResourceInfo(sender);
                    getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                });
    }

    private void start() {
        ActorRef sender = getSender();
        this.resource
                .start()
                .thenAccept(resource -> sendResourceInfo(sender));
    }


    private void connectClient(ClientInfo clientInfo) {
        if (clientInfo.id().equals(resource.parameters().id())) {
            resource.connected();
            log.info("Connected: {} {}", resource.info().parameters().providerType(), resource.info().parameters().instanceType());
        } else {
            log.error("client info and resource info don't match {}, {}", clientInfo, resource.info());
        }
        sendResourceInfo(getContext().parent());
    }

    private void sendResourceInfo(ActorRef toActor) {
        log.debug("sending {} to {}", resource.info(), toActor);
        toActor.tell(resource.info(), getSelf());
        getContext().parent().tell(resource.info(), getSelf());
    }
}
