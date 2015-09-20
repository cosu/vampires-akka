package ro.cosu.vampires.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import scala.concurrent.duration.Duration;

import java.util.Random;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * User: Cosmin 'cosu' Dumitru - cosu@cosu.ro
 * Date: 9/13/15
 * Time: 11:46 PM
 */
public class App {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ClientSystem");


        final String path = "akka.tcp://ServerSystem@127.0.0.1:2552/user/server";
        final ActorRef client = system.actorOf(ClientActor.props(path), "client");

        final ActorRef terminator = system.actorOf(Terminator.props(client), "terminator");

        system.awaitTermination();

    }
}
