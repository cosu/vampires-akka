package ro.cosu.vampires.client;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.japi.Procedure;
import akka.util.ByteString;

import java.net.InetSocketAddress;

public class ClientActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    final InetSocketAddress remote;

    public static Props props(InetSocketAddress remote) {
        return Props.create(ClientActor.class, remote);
    }

    private ByteString getMessage(){
        return ByteString.fromArray(("hello " + self().path().name()).getBytes());
    }

    public ClientActor(InetSocketAddress remote) {
        this.remote = remote;

        final ActorRef tcp = Tcp.get(getContext().system()).manager();

        tcp.tell(TcpMessage.connect(remote), getSelf());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof Tcp.CommandFailed) {
            log.info("In ro.cosu.vampires.client.ClientActor - received message: failed");
            getContext().stop(getSelf());

        } else if (msg instanceof Tcp.Connected) {

            getSender().tell(TcpMessage.register(getSelf()), getSelf());
            getContext().become(connected(getSender()));

            getSender().tell(TcpMessage.write(getMessage()), getSelf());
        }
    }

    private Procedure<Object> connected(final ActorRef connection) {
        return new Procedure<Object>() {
            @Override
            public void apply(Object msg) throws Exception {

                if (msg instanceof ByteString) {
                    connection.tell(TcpMessage.write((ByteString) msg), getSelf());

                } else if (msg instanceof Tcp.CommandFailed) {
                    // OS kernel socket buffer was full

                } else if (msg instanceof Tcp.Received) {
                    log.info("In ro.cosu.vampires.client.ClientActor - Received message: " + ((Tcp.Received) msg).data().utf8String());
                    connection.tell(TcpMessage.write(getMessage()), getSelf());

                } else if (msg instanceof Tcp.ConnectionClosed) {
                    log.info("close client");
                    getContext().stop(getSelf());
                }
            }
        };
    }

}