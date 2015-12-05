package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.*;
import ro.cosu.vampires.server.actors.Terminator;
import ro.cosu.vampires.server.actors.ResourceControl;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class Server {

    public static void main(String[] args) throws Exception {

        final ActorSystem system = ActorSystem.create("ServerSystem");

        LoggingAdapter log = Logging.getLogger(system, Server.class);

        log.info("starting actor system");

        ActorRef workActor = system.actorOf(WorkActor.props(), "workActor");


        ActorRef resourceManagerActor = system.actorOf(ResourceManagerActor.props(), "resourceManager");


        ActorRef terminator = system.actorOf(Terminator.props(), "terminator");

        system.actorOf(DispatchActor.props(workActor), "server");




        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                workActor.tell(new ResourceControl.Shutdown(), ActorRef.noSender());
                try {
                    log.info("waiting 15 seconds for shutdown");
                    Await.result(system.whenTerminated(), Duration.create("15 seconds"));

                } catch (Exception e) {
                    log.error("error during shutdown hook {}", e);
                }
            }
        });

        Await.result(system.whenTerminated(), Duration.Inf());

    }
}
