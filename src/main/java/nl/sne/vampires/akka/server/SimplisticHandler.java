package nl.sne.vampires.akka.server;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;

public class SimplisticHandler extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof Tcp.Received) {
            final ByteString data = ((Tcp.Received) msg).data();
            log.info("SimplisticHandler - Received: " + data);
            getSender().tell(TcpMessage.write(data), getSelf());
        } else if (msg instanceof Tcp.ConnectionClosed) {
            getContext().stop(getSelf());
        }
    }
}
