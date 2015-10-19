package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActorWithStash;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

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
    public  void preStart() {
        getContext().actorSelection("/user/terminator").tell(new Message.Up(), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        ActorRef sender = getSender();
        if (message instanceof Message.CreateResource) {

            Optional<Resource> resource = create((Message.CreateResource) message, sender);
            if (!resource.isPresent()) {
                getSelf().tell(Resource.Status.FAILED, sender);
            }

        } else if (message instanceof Resource.Status) {
            if (message.equals(Resource.Status.RUNNING)) {
                activate();
            } else {
                fail(sender);
            }
        }
        else {
            stash();
        }
    }

    private Optional<Resource> create(Message.CreateResource message, ActorRef sender) {

        Optional<Resource> resource = resourceProvider.create(message.parameters);

        resource.ifPresent(created -> this.resource = created);
        resource.ifPresent(created -> created.start()
                .thenAccept(started -> getSelf().tell(started.status(), sender)));

        return resource;

    }

    private Void fail(ActorRef sender) {
        log.debug("actor failed to interact with resource ");

        getContext().stop(getSelf());
        return null;
    }


    private void activate() {

        getSender().tell(resource.info(), getSelf());
        getContext().parent().tell(resource.info(), getSelf());
        log.info("activate");
        unstashAll();
        getContext().become(active);


    }

    @Override
    public void postStop() {
        if (resource != null)
            try {
                resource.stop().get();
            } catch (InterruptedException e) {
                e.printStackTrace();  //Auto-generated TODO
            } catch (ExecutionException e) {
                e.printStackTrace();  //Auto-generated TODO
            }
    }


    Procedure<Object> active = message -> {
        ActorRef sender = getSender();

        if (message instanceof Message.GetResourceInfo) {
            sender.tell(resource.info(), getSelf());
        } else if (message instanceof Message.DestroyResource) {
            log.info("destroy " + message);
            Resource result = resource.stop().get();
            sender.tell(result.info(), getSelf());

        } else {
            unhandled(message);
        }
    };
}
