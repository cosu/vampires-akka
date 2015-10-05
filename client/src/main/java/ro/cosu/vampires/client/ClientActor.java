package ro.cosu.vampires.client;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import ro.cosu.vampires.server.Message;
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

                server.tell(new Message.Up(), getSelf());

                server.tell(new Message.Request(), getSelf());

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
        if (message instanceof Message.Computation){
            ActorRef executor = getContext().actorOf(ExecutorActor.props());
            executor.tell(message , getSelf());
        }
        else if (message instanceof Message.Result) {
            // send result
            server.tell(message, getSelf());
            //send request
            server.tell(new Message.Request(), getSelf());
        }
        else {
            unhandled(message);
        }
    };







}
