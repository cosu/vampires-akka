package ro.cosu.vampires.server;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.annotations.VisibleForTesting;

import java.util.LinkedList;
import java.util.List;


public class RegisterActor extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @VisibleForTesting
    protected List<String> registered = new LinkedList<>();

    public static Props props(){
        return Props.create(RegisterActor.class);
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.Up) {

            registered.add(getSender().path().toStringWithoutAddress());
            log.info("up {}", getSender());
        }
    }
}
