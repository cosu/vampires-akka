package ro.cosu.vampires.client;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.JobStatus;
import scala.concurrent.duration.Duration;

import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ClientActor extends UntypedActor {

    private final String path;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef server;

    final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    public static Props props(String path) {
        return Props.create(ClientActor.class, path);
    }


    public ClientActor(String path) {
        this.path = path;
        sendIdentifyRequest();


    }

    private void sendIdentifyRequest() {
        getContext().actorSelection(path).tell(new Identify(path), getSelf());
        getContext()
                .system()
                .scheduler()
                .scheduleOnce(Duration.create(3, SECONDS), getSelf(),
                        ReceiveTimeout.getInstance(), getContext().dispatcher(), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof ActorIdentity) {
            server = ((ActorIdentity) message).getRef();
            if (server == null) {
                log.warning("Remote actor not available: {}", path);
            } else {
                getContext().watch(server);

                log.info("starting {} workers", settings.getParallel());
                //bootstrapping via an empty job
                IntStream.range(0, settings.getParallel())
                        .forEach(i -> execute(Job.empty()));


                getContext().become(active, true);
            }
        }else if (message instanceof ReceiveTimeout) {
            sendIdentifyRequest();
        } else {
             log.info("Not ready yet");

        }
    }

    Procedure<Object> active = message -> {

        if (message instanceof Job){

            Job job = (Job) message;

            if (JobStatus.COMPLETE.equals(job.status())) {
                server.tell(job, getSelf());
            }
            else {
                log.info("Execute {} -> {} {}" , getSelf().path() , job.computation(), getSender().toString());
                execute(job);
            }
        }
        if (message instanceof Terminated) {
            if (getSender().equals(server)){
                log.info("server left. shutting down");
                getContext().stop(getSelf());
            }

        }
        else {
            log.info("Unhandled: {} -> {} {}" , getSelf().path() , message.toString(), getSender().toString());
            unhandled(message);
        }
    };

    private void execute(Job job) {
        ActorRef executor = getContext().actorOf(ExecutorActor.props());
        executor.tell(job, getSelf());

    }



}
