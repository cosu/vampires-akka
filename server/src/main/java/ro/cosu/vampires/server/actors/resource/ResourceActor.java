package ro.cosu.vampires.server.actors.resource;


import java.util.concurrent.ExecutionException;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.QueryResource;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.values.ClientInfo;

public class ResourceActor extends AbstractActor {

    private final Resource resource;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    ResourceActor(Resource resource) {
        this.resource = resource;
    }

    public static Props props(Resource resource) {
        return Props.create(ResourceActor.class, resource);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QueryResource.class, message -> sendResourceInfo(getSender()))
                .match(ClientInfo.class, this::connectClient)
                .match(ResourceControl.Shutdown.class, message -> stop())
                .match(ResourceControl.Start.class, message -> start())
                .build();
    }


    private void stop() throws ExecutionException, InterruptedException {
        ActorRef sender = getSender();
        resource.stop()
                .thenAccept(resource ->
                {
                    sendResourceInfo(sender);
                    getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                })
                .exceptionally(this::logException);
    }

    private void start() {
        ActorRef sender = getSender();
        this.resource
                .start()
                .thenAccept(resource -> sendResourceInfo(sender))
                .exceptionally(this::logException)
        ;
    }

    private Void logException(Throwable exception) {
        log.error("Failed to stop", exception);
        return null;
    }


    private void connectClient(ClientInfo clientInfo) {
        if (clientInfo.id().equals(resource.parameters().id())) {
            resource.connected();
            log.info("Connected: {}", resource.info().parameters().resourceDescription());
        } else {
            log.error("client info and resource info don't match {}, {}", clientInfo, resource.info());
        }
        sendResourceInfo(getContext().parent());
    }

    private void sendResourceInfo(ActorRef toActor) {
        log.debug("sending {} to {}", resource.info(), toActor);
        toActor.tell(resource.info(), getSelf());
        // also update the parent
        getContext().parent().tell(resource.info(), getSelf());
    }


}
