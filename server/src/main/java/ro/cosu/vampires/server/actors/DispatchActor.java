package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Job;

public class DispatchActor extends UntypedActor {

    private final ActorRef workActor;


    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props(ActorRef workActor){
        return Props.create(DispatchActor.class, workActor);
    }

    public DispatchActor(ActorRef workActor) {
        this.workActor= workActor;
    }

    @Override
    public void onReceive(Object message) throws Exception {



        if (message instanceof Job) {
            workActor.forward(message, getContext());
        }
        else if (message instanceof ClientInfo){
            ActorRef configActor = getContext().actorOf(ConfigActor.props());
            log.debug("got client info {}", message);
            configActor.forward(message, getContext());
        }
        else {
            log.error("Unhandled  request from {} , {}",  getSender().toString(),message);
            unhandled(message);
        }

    }
}
