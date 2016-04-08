package ro.cosu.vampires.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.DispatchActor;
import ro.cosu.vampires.server.actors.ResourceControl;
import ro.cosu.vampires.server.actors.ResourceManagerActor;
import ro.cosu.vampires.server.actors.Terminator;
import ro.cosu.vampires.server.actors.WorkActor;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;


public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {

        final ActorSystem system = ActorSystem.create("ServerSystem");
        LoggingAdapter log = Logging.getLogger(system, Server.class);

        ActorRef terminator = system.actorOf(Terminator.props(), "terminator");
        ActorRef workActor = system.actorOf(WorkActor.props(), "workActor");
        system.actorOf(ResourceManagerActor.props(), "resourceManager");
        system.actorOf(DispatchActor.props(workActor), "server");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                terminator.tell(new ResourceControl.Shutdown(), ActorRef.noSender());
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
