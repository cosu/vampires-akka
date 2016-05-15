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

package ro.cosu.vampires.server.actors;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.QueryResource;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionStatus;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.writers.ResultsWriter;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ResultActor extends UntypedActor {
    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());
    private final LocalDateTime startTime = LocalDateTime.now();
    private final Execution execution;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private List<Job> results = new LinkedList<>();
    private List<ResultsWriter> writers;

    private ActorRef workActor;
    private Cancellable logSchedule;

    ResultActor(Execution execution) {
        writers = settings.getWriters();
        this.execution = execution;
    }

    public static Props props(Execution execution) {
        return Props.create(ResultActor.class, execution);
    }

    private static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }

    @Override
    public void preStart() {
        workActor = getContext().actorOf(WorkActor.props(execution), "workActor");

        logSchedule = getContext().system().scheduler().schedule(scala.concurrent.duration.Duration.Zero(),
                scala.concurrent.duration.Duration.create(30, SECONDS), () -> {
                    log.info("results so far: {}/{}", results.size(), execution.workload().getJobs().size());
                }, getContext().system().dispatcher());
    }

    @Override
    public void postStop() {
        logSchedule.cancel();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Job) {
            Job job = (Job) message;
            handleJob(job);
        } else if (message instanceof ClientInfo) {
            ClientInfo clientInfo = (ClientInfo) message;
            handleClientInfo(clientInfo);
        } else if (message instanceof QueryResource) {
            ExecutionStatus status = ExecutionStatus.fromExecution(execution).builder()
                    .total(execution.workload().size())
                    .completed(results.size())
                    .status("running")
                    .remaining(execution.workload().size() - results.size()).build();

            getSender().tell(status, getSelf());
        }
        else if (message instanceof ResourceControl.Shutdown) {
            shutdown();
        } else {
            unhandled(message);
        }
    }

    private void handleJob(Job job) {
        workActor.forward(job, getContext());
        if (!job.computation().id().equals(Computation.BACKOFF)
                && !job.computation().id().equals(Computation.EMPTY)) {
            results.add(job);
            writers.forEach(r -> r.addResult(job));
        }

        if (results.size() == execution.workload().getJobs().size()) {
            log.debug("result actor exiting");
            getContext().stop(getSelf());
        }
    }

    private void handleClientInfo(ClientInfo clientInfo) {
        ActorRef configActor = getContext().actorOf(ConfigActor.props());
        log.debug("got client info {}", clientInfo);
        configActor.forward(clientInfo, getContext());
        writers.forEach(r -> r.addClient(clientInfo));
    }

    private void shutdown() {
        log.info("Total Duration: {}", formatDuration(Duration.between(startTime, LocalDateTime.now())));
        log.info("shutting down");
        writers.forEach(ResultsWriter::close);
        // init shutdown
        getContext().stop(getSelf());
    }
}
