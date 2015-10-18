package ro.cosu.vampires.client;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import ro.cosu.vampires.server.Message;
import ro.cosu.vampires.server.workload.Result;
import ro.cosu.vampires.server.workload.Workload;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ClientActor extends UntypedActor {

    private final String path;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef server;


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

                sendMessageAndRequestNew(new Message.Up());

                getContext().become(active, true);
            }
        }else if (message instanceof ReceiveTimeout) {
            sendIdentifyRequest();
        } else {
             log.info("Not ready yet");

        }
    }

    Procedure<Object> active = message -> {
        log.info("{} -> {} {}" , getSelf().path() , message.toString(), getSender().toString());
        if (message instanceof Workload){
            Workload workload = (Workload) message;
            if (workload.result().equals(Result.empty())){
                execute(workload);
            }  else {
                sendMessageAndRequestNew(message);
            }

        }
        else {
            unhandled(message);
        }
    };

    private void execute(Workload workload) {
        ActorRef executor = getContext().actorOf(ExecutorActor.props());
        executor.tell(workload, getSelf());
    }

    private void sendMessageAndRequestNew(Object message) {
        server.tell(message, getSelf());
        server.tell(new Message.Request(), getSelf());
    }


}
