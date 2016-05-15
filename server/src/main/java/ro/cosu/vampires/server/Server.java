/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.BootstrapActor;
import ro.cosu.vampires.server.actors.Terminator;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;


public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {

        final ActorSystem system = ActorSystem.create("ServerSystem");
        LoggingAdapter log = Logging.getLogger(system, Server.class);

        ActorRef terminator = system.actorOf(Terminator.props(), "terminator");
        system.actorOf(BootstrapActor.props(terminator), "bootstrap");

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
