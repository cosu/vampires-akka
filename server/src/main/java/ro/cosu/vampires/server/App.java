package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class App {
    private static LoggingAdapter log;

    public static void main(String[] args) throws Exception {

        final ActorSystem system = ActorSystem.create("ServerSystem");


        log = Logging.getLogger(system, App.class);

        ActorRef workActor = system.actorOf(WorkActor.props(), "workActor");

        ActorRef resultActor = system.actorOf(ResultActor.props(), "resultActor");

        ActorRef registerActor = system.actorOf(RegisterActor.props(), "registerActor");

        system.actorOf(DispatchActor.props(workActor, resultActor, registerActor), "server");

        Await.result(system.whenTerminated(), Duration.Inf());


    }
}
