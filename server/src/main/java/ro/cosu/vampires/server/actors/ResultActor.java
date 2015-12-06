package ro.cosu.vampires.server.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.writers.ResultsWriter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ResultActor extends UntypedActor {
    private final int numberOfResults;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private List<Job> results = new LinkedList<>();
    private List<ClientInfo> clients = new LinkedList<>();
    List<ResultsWriter> writers;

    final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    final LocalDateTime startTime = LocalDateTime.now();


    public static Props props(int numberOfResults) {
        return Props.create(ResultActor.class, numberOfResults);
    }


    ResultActor(int numberOfResults) {
        this.numberOfResults = numberOfResults;
        writers = settings.getWriters();
    }

    @Override
    public void preStart() {
        getContext().actorSelection("/user/terminator").tell(new ResourceControl.Up(), getSelf());

        getContext().system().scheduler().schedule(scala.concurrent.duration.Duration.Zero(),
                scala.concurrent.duration.Duration.create(5 , SECONDS), () -> {
                    log.info("results so far: {}/{}", results.size(), numberOfResults);
                }, getContext().system().dispatcher());
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Job) {

            Job job = (Job) message;
            if (!Computation.empty().equals(job.computation())) {
                results.add(job);
                log.info("got result of {} . received {}/{}", job.computation().command(), results.size(),
                        numberOfResults);
                log.debug("result {}", job.result());

                writers.forEach(r -> r.addResult(job));

            }

            //this should be a predicate
            if (results.size() == numberOfResults) {
                log.info("DONE!");
                shutdown();
            }
        }
        else if (message instanceof ClientInfo) {
            clients.add((ClientInfo) message);
            writers.forEach(r -> r.addClient((ClientInfo) message));
        }
        else if (message instanceof ResourceControl.Shutdown) {
            shutdown();
        } else {
            unhandled(message);
        }
    }

    private void shutdown() {
        log.info("Total Duration: {}", formatDuration(Duration.between(startTime, LocalDateTime.now())));
        log.info("shutting down");
        writers.forEach(ResultsWriter::close);
        // init shutdown


        getContext().actorSelection("/user/terminator").tell(new ResourceControl.Shutdown(), getSelf());
        getContext().stop(getSelf());
    }

    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }

}
