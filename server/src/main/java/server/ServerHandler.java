package server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;

public class ServerHandler extends UntypedActor {

    private final ActorRef workManager;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    public static Props props (ActorRef workManager){
        return Props.create(ServerHandler.class, workManager);
    }
    public ServerHandler(ActorRef workManager) {
        this.workManager  = workManager;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof Tcp.Received) {
            final String data = ((Tcp.Received) msg).data().utf8String();
            workManager.forward(msg, context());

        } else if (msg instanceof Tcp.ConnectionClosed) {
            getContext().stop(getSelf());
        }
    }
}
