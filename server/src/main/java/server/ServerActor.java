package server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.Tcp.Bound;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.io.TcpMessage;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerActor extends UntypedActor {

    private final ActorRef workManager;
    private final int port;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private String hostname;


    public static Props props( ActorRef workManager, String  hostname, int port) {

        return Props.create(ServerActor.class, workManager,hostname, port);
    }

    public ServerActor( ActorRef workManager, String hostname, int port) {
        this.workManager= workManager;
        this.port = port;
        this.hostname = hostname;
    }

    private AtomicInteger integer = new AtomicInteger();

    @Override
    public void preStart() throws Exception {

        final ActorRef tcpActor = Tcp.get(getContext().system()).manager();


        tcpActor.tell(TcpMessage.bind(getSelf(),
                new InetSocketAddress(hostname, port), 100), getSelf());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof Bound) {
            log.info("In ServerActor - received message: bound");

        } else if (msg instanceof CommandFailed) {
            getContext().stop(getSelf());

        } else if (msg instanceof Connected) {
            log.info("In ServerActor - received message: connected " + integer.incrementAndGet());

            final ActorRef handler = getContext().actorOf(ServerHandler.props(workManager));

            getSender().tell(TcpMessage.register(handler), getSelf());

        } else if (msg instanceof Tcp.ConnectionClosed) {
            getContext().stop(getSelf());
        }
    }

    @Override
    public  void postStop(){
        System.out.println("dead" + integer.incrementAndGet());
    }


}