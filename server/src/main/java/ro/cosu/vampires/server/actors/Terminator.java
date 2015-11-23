package ro.cosu.vampires.server.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.LinkedList;
import java.util.List;

public class Terminator extends UntypedActor{

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final List<ActorRef>  refs = new LinkedList<>();


    public static Props props() {
        return Props.create(Terminator.class);
    }


    @Override
    public void onReceive(Object msg) {
        if (msg instanceof ResourceControl.Shutdown) {
           refs.forEach(r -> r.tell(PoisonPill.getInstance(), getSelf()));
        } else
        if (msg instanceof ResourceControl.Up) {
            refs.add(getSender());
            getContext().watch(getSender());
        }  else
        if (msg instanceof Terminated) {
            boolean remove = refs.remove(getSender());
            log.debug("removed {} {}", remove, getSender());
            if (refs.isEmpty()){
                getContext().system().terminate();
            } else {
                log.info("waiting for {} more", refs.size());
            }

        } else {
            unhandled(msg);
        }
    }
}
