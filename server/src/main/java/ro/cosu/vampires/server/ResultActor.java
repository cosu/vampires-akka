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
    private final int numberOfResults;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private List<Message.Result> results = new LinkedList<>();

    public static Props props(int numberOfResults){
        return Props.create(ResultActor.class, numberOfResults);
    }


    ResultActor(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }
    public  void preStart() {
        getContext().actorSelection("/user/terminator").tell(new Message.Up(), getSelf());
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.Shutdown) {


        } else if (message instanceof Message.Result) {
            results.add((Message.Result) message);
            log.debug("got result {}", message);
            if (results.size() == numberOfResults) {
                ResultsWriter writer = new JsonResultsWriter();
                writer.writeResults(results);
                getContext().stop(getSelf());
            }
        }
        else {
            unhandled(message);
        }
    }

}
