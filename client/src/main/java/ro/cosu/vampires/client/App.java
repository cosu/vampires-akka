package ro.cosu.vampires.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * User: Cosmin 'cosu' Dumitru - cosu@cosu.ro
 * Date: 9/13/15
 * Time: 11:46 PM
 */
public class App {

    private static LoggingAdapter log;

    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create("ClientSystem");

        log = Logging.getLogger(system, App.class);


        String host = system.settings().config().getString("vampires.server_ip");

        final String path = "akka.tcp://ServerSystem@" + host + ":2552/user/server";

        log.info("server path {}", path);


        final ActorRef client = system.actorOf(ClientActor.props(path), "client");

        final ActorRef terminator = system.actorOf(Terminator.props(client), "terminator");

        system.awaitTermination();

    }
}
