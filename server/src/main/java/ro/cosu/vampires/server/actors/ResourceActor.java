package ro.cosu.vampires.server.actors;

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

	public static Props props(ResourceProvider resourceProvider) {
		return Props.create(ResourceActor.class, resourceProvider);
	}

	ResourceActor(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;

	}

	@Override
	public void preStart() {
		getContext().actorSelection("/user/terminator").tell(new ResourceControl.Up(), getSelf());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		ActorRef sender = getSender();
		if (message instanceof ResourceControl.Create) {


			Optional<Resource> resource = create((ResourceControl.Create) message, sender);
			if (!resource.isPresent()) {
				getSelf().tell(Resource.Status.FAILED, sender);
			}

		} else if (message instanceof Resource.Status) {
			if (message.equals(Resource.Status.RUNNING)) {
				activate();
			} else {
				fail(sender);
			}
		} else {
			log.debug("stash {}", message);
			stash();
		}
	}

	private Optional<Resource> create(ResourceControl.Create create, ActorRef sender) {

		Optional<Resource> resource = resourceProvider.create(create.parameters);

		resource.ifPresent(created -> this.resource = created);
		resource.ifPresent(created -> created.start()
				.thenAccept(started -> {
					log.debug("started!!");
					getSelf().tell(started.status(), sender);
				}));

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
		unstashAll();
		getContext().become(active);


	}

	@Override
	public void postStop() {
		if (resource != null)
			try {
				resource.stop().get();
			} catch (InterruptedException | ExecutionException e) {
				log.error("failed to stop resource {}", e);
			}
	}


	Procedure<Object> active = message -> {
		ActorRef sender = getSender();

		if (message instanceof ResourceControl.Info) {
			sender.tell(resource.info(), getSelf());
		} else if (message instanceof ResourceControl.Shutdown) {
			log.debug("shutdown " + message);
			Resource stoppedResource = resource.stop().get();
			sender.tell(stoppedResource.info(), getSelf());
		} else {
			unhandled(message);
		}
	};
}
