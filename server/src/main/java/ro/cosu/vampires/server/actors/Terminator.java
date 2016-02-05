package ro.cosu.vampires.server.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.LinkedList;
import java.util.List;

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
            log.debug("new watch {}", sender);
            refs.add(sender);
            getContext().watch(sender);
        } else if (msg instanceof Terminated) {
            boolean remove = refs.remove(sender);
            log.debug("removed {} {}", remove, sender);
            if (refs.isEmpty()) {
                log.debug("shutting down system");
                getContext().system().terminate();
            } else {
                log.info("waiting for {} more", refs.size());
            }

        } else {
            unhandled(msg);
        }
    }
}
