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

package ro.cosu.vampires.client.actors;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Identify;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import akka.pattern.Patterns;
import akka.util.Timeout;
import ro.cosu.vampires.client.extension.ExecutorsExtension;
import ro.cosu.vampires.client.extension.ExecutorsExtensionImpl;
import ro.cosu.vampires.server.values.ClientConfig;
import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.JobStatus;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ClientActor extends UntypedActor {

    private final String serverPath;
    private final ExecutorsExtensionImpl executors = ExecutorsExtension.ExecutorsProvider.get(getContext().system());
    private String clientId;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef server;
    private Procedure<Object> active = message -> {
        if (message instanceof Job) {
            handleJob((Job) message);
        } else if (message instanceof Terminated) {
            if (getSender().equals(server)) {
                log.info("server left. shutting down");
                getContext().stop(getSelf());
            }
        } else {
            log.error("Unhandled: {} -> {} {}", getSelf().path(), message.toString(), getSender());
            unhandled(message);
        }
    };
    private Procedure<Object> waitForConfig = message -> {
        if (message instanceof ClientConfig) {
            ClientConfig config = (ClientConfig) message;
            executors.configure(config);
            log.info("starting {} workers", config.numberOfExecutors());
            //bootstrapping via an empty job
            IntStream.range(0, config.numberOfExecutors()).forEach(i -> execute(Job.empty()));
            getContext().setReceiveTimeout(Duration.create(1, TimeUnit.SECONDS));
            getContext().become(active, true);
        } else {
            unhandled(message);
        }
    };

    public ClientActor(String serverPath, String clientId) {
        this.serverPath = serverPath;
        this.clientId = clientId;
        sendIdentifyRequest();
    }

    public static Props props(String path, String clientId) {
        return Props.create(ClientActor.class, path, clientId);
    }

    private void handleJob(Job job) {
        if (JobStatus.COMPLETE.equals(job.status())) {
            server.tell(job.from(clientId), getSelf());
        } else {
            log.debug("Execute {} -> {} {}", getSelf().path(), job.computation(), getSender());
            execute(job);
        }
    }

    private void sendIdentifyRequest() {

        log.info("connecting to {}", serverPath);
        getContext().actorSelection(serverPath).tell(new Identify(serverPath), getSelf());
        getContext().system().scheduler()
                .scheduleOnce(
                        Duration.create(3, SECONDS), getSelf(),
                        ReceiveTimeout.getInstance(), getContext().dispatcher(), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof ActorIdentity) {
            server = ((ActorIdentity) message).getRef();
            if (server == null) {
                log.warning("Remote actor not available: {}", serverPath);
            } else {
                getContext().watch(server);
                server.tell(getClientInfo(), getSelf());
                getContext().become(waitForConfig, true);
            }
        } else if (message instanceof ReceiveTimeout) {
            sendIdentifyRequest();
        } else {
            log.info("Not ready yet");
        }
    }

    private void execute(Job job) {
        ActorRef executorActor = getContext().actorOf(ExecutorActor.props());
        executorActor.tell(job, getSelf());
    }

    private ClientInfo getClientInfo() throws Exception {
        final Map<String, Integer> executorInfo = executors.getExecutorInfo();

        final ActorSelection monitorActor = getContext().actorSelection("/user/monitor");

        // ugly but this is done only when the client boots
        final Future<Object> metricsFuture = Patterns.ask(monitorActor, Metrics.empty(), Timeout.apply(1, SECONDS));
        Metrics metrics = (Metrics) Await.result(metricsFuture, Duration.create("2 seconds"));

        return ClientInfo.builder().id(clientId).executors(executorInfo).metrics(metrics).build();
    }


}
