package server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;


public class WorkManager extends UntypedActor {
    private final ConcurrentLinkedQueue workQueue;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props(ConcurrentLinkedQueue workQueue){
        return Props.create(WorkManager.class, workQueue);
    }

    public WorkManager(ConcurrentLinkedQueue workQueue) {
        this.workQueue = workQueue;

    }

    @Override
    public void onReceive(Object msg) throws Exception {
        log.info("In workManager " + getSender().toString() + " " + msg.getClass().toString());
        if (msg instanceof Tcp.Received) {

            Object work = Optional.ofNullable(workQueue.poll())
                    .map(Object::toString)
                    .map(String::getBytes)
                    .map(ByteString::fromArray)
                    .map(TcpMessage::write)
                    .orElse(TcpMessage.confirmedClose());

            getSender().tell(work, getSelf());
        }
    }
}
