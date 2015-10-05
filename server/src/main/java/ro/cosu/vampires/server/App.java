package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class App {
    private static LoggingAdapter log;

    public static void main(String[] args) {


        final ActorSystem system = ActorSystem.create("ServerSystem");


        log = Logging.getLogger(system, App.class);

        ActorRef workActor = system.actorOf(WorkActor.props(), "workActor");

        ActorRef resultActor = system.actorOf(ResultActor.props(), "resultActor");

        ActorRef registerActor = system.actorOf(RegisterActor.props(), "registerActor");

        system.actorOf(DispatchActor.props(workActor, resultActor, registerActor), "server");



    }
}
