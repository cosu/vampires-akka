package nl.sne.vampires.akka.client;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.japi.Procedure;
import akka.util.ByteString;

import java.net.InetSocketAddress;

public class ClientActor extends UntypedActor {

    private final InetSocketAddress remote;
    private final ActorRef listener;



    public ClientActor( InetSocketAddress remote, ActorRef listener){

        this.remote = remote;
        this.listener = listener;

        final ActorRef tcp = Tcp.get(getContext().system()).manager();
        tcp.tell(TcpMessage.connect(remote), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Tcp.CommandFailed) {
            listener.tell("failed", getSelf());
            getContext().stop(getSelf());
        }
        else if (message instanceof Tcp.Connected) {
            listener.tell(message, getSelf());
            getSender().tell(TcpMessage.register(getSelf()), getSender());
            getContext().become(connected(getSender()));
        }
    }

    private Procedure<Object> connected(final ActorRef connection) {
        return new Procedure<Object>()  {

            @Override
            public void apply(Object message) throws Exception {
                if ( message instanceof ByteString){
                    connection.tell(TcpMessage.write((ByteString) message), getSelf());
                } else if (message instanceof Tcp.CommandFailed) {
                    // socket buffer full
                } else if (message instanceof Tcp.Received) {
                    listener.tell(((Tcp.Received) message).data(), getSelf());

                } else if (message.equals("close")) {
                    connection.tell(TcpMessage.close(), getSelf());

                } else if (message instanceof Tcp.ConnectionClosed) {
                    getContext().stop(getSelf());
                }
            }
        };
    }
}
