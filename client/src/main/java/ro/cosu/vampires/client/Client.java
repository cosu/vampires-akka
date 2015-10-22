package ro.cosu.vampires.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.slf4j.bridge.SLF4JBridgeHandler;
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

        ActorSystem system = ActorSystem.create("ClientSystem");

        String host = system.settings().config().getString("vampires.server_ip");

        final String path = "akka.tcp://ServerSystem@" + host + ":2552/user/server";



        final ActorRef monitor = system.actorOf(MonitoringActor.props(MonitoringManager.getMetricRegistry()), "monitor");

        final ActorRef client = system.actorOf(ClientActor.props(path), "client");
        final ActorRef terminator = system.actorOf(Terminator.props(client), "terminator");


        Await.result(system.whenTerminated(), Duration.Inf());

    }

}
