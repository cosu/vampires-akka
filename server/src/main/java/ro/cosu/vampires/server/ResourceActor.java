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
        ActorRef sender = getSender();
        if (message instanceof Message.CreateResource) {
            resource = resourceProvider.create(((Message.CreateResource) message).parameters);

            resource.start().thenAccept(resource -> getSelf().tell(resource, sender));
        } else if (message instanceof Resource) {
            if (((Resource) message).getStatus().equals(Resource.Status.RUNNING)) {
                activate();
            } else {
                fail(sender);
            }
        }
        else {
            stash();
        }
    }

    private Void fail(ActorRef sender) {
        log.debug("actor failed to interact with resource {}", resource.getInfo());
        sender.tell(resource.getInfo(), getSelf());
        getContext().stop(getSelf());
        return null;
    }


    private void activate() {
        getSender().tell(resource.getInfo(), getSelf());
        getContext().parent().tell(resource.getInfo(), getSelf());
        log.info("activate");
        unstashAll();
        getContext().become(active);


    }


    Procedure<Object> active = message -> {

        log.info("ResourceActor {} -> {} {}", getSelf().path(), message.toString(), getSender().toString());
        if (message instanceof Message.GetResourceInfo) {
            if (((Message.GetResourceInfo) message).resourceDescription.equals(resource.getDescription()))
                getSender().tell(resource.getInfo(), getSelf());
        } else if (message instanceof Message.DestroyResource) {
            if (((Message.DestroyResource) message).resourceDescription.equals(resource.getDescription())){
                ActorRef sender = getSender();
                resource.stop().thenAccept(result ->  sender.tell(result.getInfo(), getSelf()));
            }
        } else {
            unhandled(message);
        }
    };
}
