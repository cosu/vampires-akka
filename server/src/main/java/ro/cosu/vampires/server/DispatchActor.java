package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class DispatchActor extends UntypedActor {

    private final ActorRef workActor;

    private final ActorRef registerActor;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props(ActorRef workActor, ActorRef registerActor){
        return Props.create(DispatchActor.class, workActor, registerActor);
    }

    public DispatchActor(ActorRef workActor, ActorRef registerActor) {
        this.workActor= workActor;
        this.registerActor = registerActor;

    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Message.Request) {
            workActor.forward(message, getContext());
        } else if (message instanceof Message.Result) {
            workActor.forward(message, getContext());
        } else if (message instanceof Message.Up){
            registerActor.forward(message, getContext());
            }
        else {
            log.error("Unhandled  request from {} , {}",  getSender().toString(),message);
            unhandled(message);
        }

    }
}
