package ro.cosu.vampires.client;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import ro.cosu.vampires.client.actors.ClientActor;
import ro.cosu.vampires.client.actors.MonitoringActor;
import ro.cosu.vampires.client.actors.TerminatorActor;
import ro.cosu.vampires.client.monitoring.MonitoringManager;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class Client {

    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    public static void main(String[] args) throws Exception {
        String host;
        String clientId;
        ActorSystem system = ActorSystem.create("ClientSystem");

        if (args.length == 2) {
            host = args[0];
            clientId = args[1];
        } else {
            throw new IllegalArgumentException("missing client id");
        }

        final String serverPath = "akka.tcp://ServerSystem@" + host + ":2552/user/server";

        system.actorOf(MonitoringActor.props(MonitoringManager.getMetricRegistry()), "monitor");
        final ActorRef client = system.actorOf(ClientActor.props(serverPath, clientId), "client");
        system.actorOf(TerminatorActor.props(client), "terminator");

        Await.result(system.whenTerminated(), Duration.Inf());
    }
}
