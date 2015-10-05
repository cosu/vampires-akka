package ro.cosu.vampires.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * User: Cosmin 'cosu' Dumitru - cosu@cosu.ro
 * Date: 9/13/15
 * Time: 11:46 PM
 */
public class App {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ClientSystem");

        String host = args.length == 1 ? args[0] : "localhost";
        final String path = "akka.tcp://ServerSystem@" + host + ":2552/user/server";
        final ActorRef client = system.actorOf(ClientActor.props(path), "client");

        final ActorRef terminator = system.actorOf(Terminator.props(client), "terminator");

        system.awaitTermination();

    }
}
