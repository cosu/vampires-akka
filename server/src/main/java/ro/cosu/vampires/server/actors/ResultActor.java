package ro.cosu.vampires.server.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.writers.ResultsWriter;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class ResultActor extends UntypedActor{
    private final int numberOfResults;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private List<Job> results = new LinkedList<>();
    List<ResultsWriter> writers;

    final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    final LocalDateTime startTime = LocalDateTime.now();


    public static Props props(int numberOfResults){
        return Props.create(ResultActor.class, numberOfResults);
    }


    ResultActor(int numberOfResults) {
        this.numberOfResults = numberOfResults;
        writers = settings.getWriters();
    }

    @Override
    public  void preStart() {
        getContext().actorSelection("/user/terminator").tell(new ResourceControl.Up(), getSelf());

    }



    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Job) {

            Job job = (Job) message;
            if (!Computation.empty().equals(job.computation())) {
                results.add(job);
                log.info("got result of {} . received {}/{}",job.computation().command(), results.size(),
                        numberOfResults);
                log.debug("result {}", job.result());

                writers.forEach(r -> r.writeResult(job));
            }

            //this should be a predicate
            if (results.size() == numberOfResults) {
                log.info("DONE!");
                log.info("Total Duration: {}", java.time.Duration.between(startTime, LocalDateTime.now()));
                shutdown();
            }
        }
        if (message instanceof ResourceControl.Shutdown) {
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
        getContext().actorSelection("/user/terminator").tell(new ResourceControl.Shutdown(), getSelf());
        getContext().stop(getSelf());
    }

}
