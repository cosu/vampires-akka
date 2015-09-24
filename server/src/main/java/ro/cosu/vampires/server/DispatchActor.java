package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class DispatchActor extends UntypedActor {

    private final ActorRef workActor;
    private final ActorRef resultActor;
    private final ActorRef registerActor;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props(ActorRef workActor, ActorRef resultActor, ActorRef registerActor){
        return Props.create(DispatchActor.class, workActor, resultActor , registerActor);
    }

    public DispatchActor(ActorRef workActor, ActorRef resultActor, ActorRef registerActor) {
        this.workActor= workActor;
        this.resultActor = resultActor;
        this.registerActor = registerActor;

    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Message.Request) {
            workActor.forward(message, getContext());
        } else if (message instanceof Message.Result) {
            resultActor.forward(message, getContext());
        } else if (message instanceof Message.Up){
            registerActor.forward(message, getContext());
            }
        else {
            log.error("Unhandled work request from {} , {}",  getSender().toString(),message);
            unhandled(message);
        }

    }
}
