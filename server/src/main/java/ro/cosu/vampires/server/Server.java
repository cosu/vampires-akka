package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.actors.*;
import ro.cosu.vampires.server.actors.Terminator;
import ro.cosu.vampires.server.util.Ip;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import static spark.Spark.*;

public class Server {

    static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private static void runSpark(){
        port(8080);
        webSocket("/",WebsocketHandler.class);
        staticFileLocation("/public");
        LOG.info("starting spark server");
        init();

    }

    public static void main(String[] args) throws Exception {

//        runSpark();

        final ActorSystem system = ActorSystem.create("ServerSystem");
        LoggingAdapter log = Logging.getLogger(system, Server.class);
        system.actorOf(Terminator.props(), "terminator");
        ActorRef workActor = system.actorOf(WorkActor.props(), "workActor");
        system.actorOf(ResourceManagerActor.props(), "resourceManager");
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
        stop();
    }
}
