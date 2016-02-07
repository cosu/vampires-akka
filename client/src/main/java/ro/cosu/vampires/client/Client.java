package ro.cosu.vampires.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.slf4j.bridge.SLF4JBridgeHandler;
import ro.cosu.vampires.client.actors.ClientActor;
import ro.cosu.vampires.client.actors.MonitoringActor;
import ro.cosu.vampires.client.actors.TerminatorActor;
import ro.cosu.vampires.client.monitoring.MonitoringManager;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.logging.LogManager;

/**
 * User: Cosmin 'cosu' Dumitru - cosu@cosu.ro
 * Date: 9/13/15
 * Time: 11:46 PM
 */
public class Client {

    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw  new IllegalArgumentException("missing client id");
        }

        String clientId = args[0];
        ActorSystem system = ActorSystem.create("ClientSystem");

        String host = system.settings().config().getString("vampires.server_ip");
        final String serverPath = "akka.tcp://ServerSystem@" + host + ":2552/user/server";

        system.actorOf(MonitoringActor.props(MonitoringManager.getMetricRegistry()), "monitor");
        final ActorRef client = system.actorOf(ClientActor.props(serverPath, clientId), "client");
        system.actorOf(TerminatorActor.props(client), "terminator");

        Await.result(system.whenTerminated(), Duration.Inf());
    }
}
