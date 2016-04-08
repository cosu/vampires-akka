package ro.cosu.vampires.server.actors;

import java.util.LinkedList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Terminator extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final List<ActorRef> refs = new LinkedList<>();


    public static Props props() {
        return Props.create(Terminator.class);
    }


    @Override
    public void onReceive(Object msg) {
        ActorRef sender = getSender();
        if (msg instanceof ResourceControl.Shutdown) {
            refs.forEach(r -> r.tell(PoisonPill.getInstance(), getSelf()));
        } else if (msg instanceof ResourceControl.Up) {
            refs.add(sender);
            getContext().watch(sender);
        } else if (msg instanceof Terminated) {
            refs.remove(sender);
            if (refs.isEmpty()) {
                log.info("shutting down system");
                getContext().system().terminate();
            } else {
                log.info("waiting for {} more", refs.size());
            }

        } else {
            unhandled(msg);
        }
    }
}
