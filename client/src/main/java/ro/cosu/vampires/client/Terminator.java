package ro.cosu.vampires.client;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;


public class Terminator extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final ActorRef ref;


    public static Props props(ActorRef ref) {
        return Props.create(Terminator.class, ref);
    }

    public Terminator(ActorRef ref) {
        this.ref = ref;
        getContext().watch(ref);
    }

    @Override
    public void onReceive(Object msg) {
        if (msg instanceof Terminated) {
            log.info("{} has terminated, shutting down system", ref.path());
            getContext().system().shutdown();
        } else {
            unhandled(msg);
        }
    }

}
