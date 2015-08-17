package nl.sne.vampires.akka.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.TcpMessage;

import java.net.InetSocketAddress;

public class ServerActor extends UntypedActor {

    final ActorRef manager;

    public ServerActor(ActorRef manager) {
        this.manager = manager;
    }

    public static Props props(ActorRef manager) {
        return Props.create(ServerActor.class, manager);
    }

    @Override
    public void preStart() throws Exception {
        final ActorRef tcp = Tcp.get(getContext().system()).manager();
        tcp.tell(TcpMessage.bind(getSelf(),
                new InetSocketAddress("localhost", 0), 100), getSelf());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof Tcp.Bound) {
            manager.tell(msg, getSelf());

        } else if (msg instanceof Tcp.CommandFailed) {
            getContext().stop(getSelf());

        } else if (msg instanceof Tcp.Connected) {
            final Tcp.Connected conn = (Tcp.Connected) msg;
            manager.tell(conn, getSelf());
            final ActorRef handler = getContext().actorOf(
                    Props.create(SimplisticHandler.class));
            getSender().tell(TcpMessage.register(handler), getSelf());
        }
    }
}
