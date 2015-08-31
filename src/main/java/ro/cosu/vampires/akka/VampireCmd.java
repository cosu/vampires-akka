package ro.cosu.vampires.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import ro.cosu.vampires.akka.client.ClientActor;
import ro.cosu.vampires.akka.server.ServerActor;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VampireCmd {
    public static void main(String[] args) {

        ActorSystem serverActorSystem = ActorSystem.create("ServerActorSystem");

        ActorRef serverActor = serverActorSystem.actorOf(ServerActor.props(null), "serverActor");

        final ActorSystem clientActorSystem = ActorSystem.create("ClientActorSystem");


        List<ActorRef> actors = IntStream.of(10).mapToObj(id ->
                clientActorSystem.actorOf(ClientActor.props(new InetSocketAddress("localhost", 9090), null), "clientActor" + id))
                .collect(Collectors.toList());

        actors.stream().map(ActorRef::path).forEach(System.out::println);

        serverActorSystem.awaitTermination();
        clientActorSystem.awaitTermination();


    }
}
