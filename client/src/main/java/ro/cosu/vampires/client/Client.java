/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

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
        String serverId;
        ActorSystem system = ActorSystem.create("ClientSystem");

        if (args.length == 3) {
            host = args[0];
            serverId = args[1];
            clientId = args[2];
        } else {
            throw new IllegalArgumentException("missing client id");
        }

        final String serverPath = "akka.tcp://ServerSystem@" + host + ":2552/user/bootstrap/" + serverId;

        system.actorOf(MonitoringActor.props(MonitoringManager.getMetricRegistry()), "monitor");
        final ActorRef client = system.actorOf(ClientActor.props(serverPath, clientId), "client");
        system.actorOf(TerminatorActor.props(client), "terminator");

        Await.result(system.whenTerminated(), Duration.Inf());
    }
}
