package ro.cosu.vampires.server;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ResultActor extends UntypedActor{
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    public static Props props(){
        return Props.create(ResultActor.class);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.Result) {
            log.info("got result {}", message);
        }
    }
}
