package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;


import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;

public class App {
    private static LoggingAdapter log;

    public static void main(String[] args) {

        int port = 9090;
        String hostname = "localhost";



        final ActorSystem system = ActorSystem.create("ServerSystem");

        log = Logging.getLogger(system, App.class);

        ActorRef work = system.actorOf(WorkActor.props(), "work");

        ActorRef resultActor = system.actorOf(ResultActor.props(), "result");

        system.actorOf(DispatchActor.props(work, resultActor), "server");



    }
}
