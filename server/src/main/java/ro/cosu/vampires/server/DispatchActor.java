package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class DispatchActor extends UntypedActor {

    private final ActorRef workActor;
    private final ActorRef resultActor;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props(ActorRef workActor, ActorRef resultActor){
        return Props.create(DispatchActor.class, workActor, resultActor);
    }

    public DispatchActor(ActorRef workActor, ActorRef resultActor) {
        this.workActor= workActor;
        this.resultActor = resultActor;

    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("Work request from {} , {}",  getSender().toString(),message.getClass().toString() );

        if (message instanceof Message.Request) {
            Message.Request msg = (Message.Request) message;
            log.info("got request {}" , msg);
            workActor.forward(message, getContext());
        } else if (message instanceof Message.Result) {
            Message.Result msg= (Message.Result) message;
            resultActor.forward(message, getContext());
            log.info("got result {}",  message.toString());
        } else {
            log.error("Unhandled work request from {} , {}",  getSender().toString(),message.getClass().toString() );
            unhandled(message);
        }

    }
}
