package server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;


import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VampireCmd {
    public static void main(String[] args) {

        ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();

        int port = 9090;
        String hostname = "locgalhost";

        IntStream.range(1, 3).forEach(queue::add);

        queue.stream().forEach(System.out::print);


    }
}
