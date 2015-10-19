package ro.cosu.vampires.server;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.writers.ResultsWriter;

import java.util.LinkedList;
import java.util.List;

public class ResultActor extends UntypedActor{
    private final int numberOfResults;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private List<Workload> results = new LinkedList<>();
    List<ResultsWriter> writers;

    final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());


    public static Props props(int numberOfResults){
        return Props.create(ResultActor.class, numberOfResults);
    }


    ResultActor(int numberOfResults) {
        this.numberOfResults = numberOfResults;
        writers = settings.getWriters();
    }

    @Override
    public  void preStart() {
        getContext().actorSelection("/user/terminator").tell(new Message.Up(), getSelf());
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Workload) {
            results.add((Workload) message);
            log.debug("got result {}", message);

            writers.forEach(r -> r.writeResult((Workload)message));

            //this should be a predicate
            if (results.size() == numberOfResults) {
                shutdown();
            }
        }
        if (message instanceof Message.Shutdown) {
            shutdown();
        }
        else {
            unhandled(message);
        }
    }

    private void shutdown() {
        log.info("shutting down");
        writers.forEach(ResultsWriter::close);
        // init shutdown
        getContext().actorSelection("/user/registerActor").tell(new Message.Shutdown(), getSelf());
        getContext().stop(getSelf());
    }

}
