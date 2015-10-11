package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActorWithStash;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;

public class ResourceActor extends UntypedActorWithStash {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    private final ResourceProvider resourceProvider;
    private Resource resource;

    public static Props props(ResourceProvider resourceProvider){
        return Props.create(ResourceActor.class, resourceProvider);
    }

    ResourceActor(ResourceProvider resourceProvider){
        this.resourceProvider= resourceProvider;

    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.CreateResource) {
            resource = resourceProvider.create();

            ActorRef sender = getSender();
            resource.start().thenRun(() -> this.activate(sender))
                    .exceptionally(this::fail);
        } else {
            stash();
        }
    }

    private Void fail(Throwable ex) {
        log.debug("actor failed to interact with resource {}", ex);
        getContext().stop(getSelf());
        return null;
    }


    private void activate(ActorRef sender) {
        unstashAll();
        sender.tell(resource.getInfo(), getSelf());
        getContext().become(active);
    }


    Procedure<Object> active = message -> {

        log.info("ResourceActor {} -> {} {}", getSelf().path(), message.toString(), getSender().toString());
        if (message instanceof Message.GetResourceDescription) {
            getSender().tell(resource.getInfo(), getSelf());
        } else if (message instanceof Message.DestroyResource) {
            ActorRef sender = getSender();
            resource.stop().thenAccept(result -> sender.tell(result.getInfo(), getSelf()));
        } else {
            unhandled(message);
        }
    };
}
