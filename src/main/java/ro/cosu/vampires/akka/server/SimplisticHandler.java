package ro.cosu.vampires.akka.server;

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
            final String data = ((Tcp.Received) msg).data().utf8String();
            log.info("In SimplisticHandlerActor - Received message: " + data);
            getSender().tell(TcpMessage.write(ByteString.fromArray(("echo "+data).getBytes())), getSelf());
        } else if (msg instanceof Tcp.ConnectionClosed) {
            getContext().stop(getSelf());
        }
    }
}
