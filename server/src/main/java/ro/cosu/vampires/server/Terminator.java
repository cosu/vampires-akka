package ro.cosu.vampires.server;

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
        if (msg instanceof Message.Shutdown) {
           refs.forEach(r -> r.tell(PoisonPill.getInstance(), getSelf()));
        } else
        if (msg instanceof Message.Up) {
            refs.add(getSender());
            getContext().watch(getSender());
        }  else
        if (msg instanceof Terminated) {
            boolean remove = refs.remove(getSender());
            log.info("removed {} {}", remove, getSender());
            if (refs.isEmpty()){
                getContext().system().terminate();
            }

        } else {
            unhandled(msg);
        }
    }
}
