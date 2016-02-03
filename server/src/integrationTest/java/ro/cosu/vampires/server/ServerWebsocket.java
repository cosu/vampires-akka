package ro.cosu.vampires.server;

import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

public class ServerWebsocket {
    static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private static void runSpark(){
        port(8080);
        webSocket("/",WebsocketHandler.class);
        staticFileLocation("/public");
        LOG.info("starting spark server");
        init();
    }

    public static void main(String[] args) {
        runSpark();
        final ActorSystem system = ActorSystem.create("ServerSystem");
        system.scheduler().schedule(Duration.Zero(), Duration.create(1, TimeUnit.SECONDS),
                ()->{
                    WebsocketHandler.broadcastMessage("foo", "bar");
                }, system.dispatcher());

    }
}