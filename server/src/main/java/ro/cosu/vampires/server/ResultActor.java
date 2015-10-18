package ro.cosu.vampires.server;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.writers.json.JsonResultsWriter;
import ro.cosu.vampires.server.writers.ResultsWriter;
import ro.cosu.vampires.server.workload.Workload;

import java.util.LinkedList;
import java.util.List;

public class ResultActor extends UntypedActor{
    private final int numberOfResults;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private List<Workload> results = new LinkedList<>();
    ResultsWriter writer = new JsonResultsWriter();


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
        if (message instanceof Workload) {
            results.add((Workload) message);
            log.debug("got result {}", message);
            writer.writeResult((Workload)message);

            //this should be a predicate
            if (results.size() == numberOfResults) {

                writer.close();
                // init shutdown
                getContext().actorSelection("/user/registerActor").tell(new Message.Shutdown(), getSelf());
                getContext().stop(getSelf());
            }
        }
        else {
            unhandled(message);
        }
    }

}
