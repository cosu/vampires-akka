package ro.cosu.vampires.client.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import akka.pattern.Patterns;
import akka.util.Timeout;
import ro.cosu.vampires.client.extension.ExecutorsExtension;
import ro.cosu.vampires.client.extension.ExecutorsExtensionImpl;
import ro.cosu.vampires.server.workload.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Map;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ClientActor extends UntypedActor {

    private final String serverPath;
    private  String clientId ;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef server;
    private final ExecutorsExtensionImpl executors = ExecutorsExtension.ExecutorsProvider.get(getContext().system());

    public static Props props(String path, String clientId) {
        return Props.create(ClientActor.class, path, clientId);
    }

    public ClientActor(String serverPath, String clientId) {
        this.serverPath = serverPath;
        this.clientId = clientId;
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
                server.tell(getClientInfo(), getSelf());
                getContext().become(waitForConfig, true);
            }
        } else if (message instanceof ReceiveTimeout) {
            sendIdentifyRequest();
        } else {
            log.info("Not ready yet");
        }
    }

    private Procedure<Object> active = message -> {
        if (message instanceof Job) {
            Job job = (Job) message;
            if (JobStatus.COMPLETE.equals(job.status())) {
                server.tell(job.from(clientId), getSelf());
            } else {
                log.debug("Execute {} -> {} {}", getSelf().path(), job.computation(), getSender());
                execute(job);
            }
        } else if (message instanceof Terminated) {
            if (getSender().equals(server)) {
                log.info("server left. shutting down");
                getContext().stop(getSelf());
            }
        } else {
            log.error("Unhandled: {} -> {} {}", getSelf().path(), message.toString(), getSender());
            unhandled(message);
        }
    };

    private Procedure<Object> waitForConfig = messsage -> {
        if (messsage instanceof ClientConfig) {
            ClientConfig config = (ClientConfig) messsage;
            executors.configure(config);
            log.info("starting {} workers", config.numberOfExecutors());
            //bootstrapping via an empty job
            IntStream.range(0, config.numberOfExecutors()).forEach(i -> execute(Job.empty()));

            getContext().become(active, true);
        } else {
            unhandled(messsage);
        }
    };

    private void execute(Job job) {
        if (!job.equals(Job.waitForever())) {
            ActorRef executorActor = getContext().actorOf(ExecutorActor.props());
            executorActor.tell(job, getSelf());
        }
    }

    private ClientInfo getClientInfo() throws Exception {
        final Map<String, Integer> executorInfo = executors.getExecutorInfo();

        final ActorSelection monitorActor = getContext().actorSelection("/user/monitor");

        final Future<Object> metricsFuture = Patterns.ask(monitorActor, Metrics.empty(), Timeout.apply(1, SECONDS));
        Metrics metrics = (Metrics) Await.result(metricsFuture, Duration.create("5 seconds"));


        return ClientInfo.builder().id(clientId).executors(executorInfo).metrics(metrics).build();
    }


}
