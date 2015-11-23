package ro.cosu.vampires.client.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import ro.cosu.vampires.client.extension.ExecutorsExtension;
import ro.cosu.vampires.client.extension.ExecutorsExtensionImpl;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.JobStatus;
import scala.concurrent.duration.Duration;

import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ClientActor extends UntypedActor {

    private final String serverPath;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef server;
    private final ExecutorsExtensionImpl executors = ExecutorsExtension.ExecutorsProvider.get(getContext().system());


    public static Props props(String path) {
        return Props.create(ClientActor.class, path);
    }


    public ClientActor(String serverPath) {
        this.serverPath = serverPath;
        sendIdentifyRequest();

    }

    private void sendIdentifyRequest() {

        log.info("connecting to {}", serverPath);
        getContext().actorSelection(serverPath).tell(new Identify(serverPath), getSelf());
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
                log.warning("Remote actor not available: {}", serverPath);
            } else {
                getContext().watch(server);

                int numberOfExecutors = executors.getExecutor().getNCpu();
                log.info("starting {} workers", numberOfExecutors);
                //bootstrapping via an empty job
                IntStream.range(0, numberOfExecutors).forEach(i -> execute(Job.empty()));


                getContext().become(active, true);
            }
        } else if (message instanceof ReceiveTimeout) {
            sendIdentifyRequest();
        } else {
            log.info("Not ready yet");

        }
    }

    Procedure<Object> active = message -> {

        if (message instanceof Job) {

            Job job = (Job) message;

            if (JobStatus.COMPLETE.equals(job.status())) {
                server.tell(job, getSelf());
            } else {
                log.debug("Execute {} -> {} {}", getSelf().path(), job.computation(), getSender().toString());
                execute(job);
            }
        } else if (message instanceof Terminated) {
            if (getSender().equals(server)) {
                log.info("server left. shutting down");
                getContext().stop(getSelf());
            }

        } else {
            log.error("Unhandled: {} -> {} {}", getSelf().path(), message.toString(), getSender().toString());
            unhandled(message);
        }
    };

    private void execute(Job job) {
        ActorRef executorActor = getContext().actorOf(ExecutorActor.props());
        executorActor.tell(job, getSelf());

    }


}
