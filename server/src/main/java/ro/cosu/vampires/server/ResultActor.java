package ro.cosu.vampires.server;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.util.JsonResultsWriter;
import ro.cosu.vampires.server.util.ResultsWriter;

import java.util.LinkedList;
import java.util.List;

public class ResultActor extends UntypedActor{
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private List<Message.Result> results = new LinkedList<>();

    public static Props props(){
        return Props.create(ResultActor.class);
    }


    public  void preStart() {
        getContext().actorSelection("/user/terminator").tell(new Message.Up(), getSelf());
    }

    @Override
    public void postStop(){
        ResultsWriter writer = new JsonResultsWriter();
        writer.writeResults(results);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.Result) {
            results.add((Message.Result) message);
            log.debug("got result {}", message);
        }
    }

}
