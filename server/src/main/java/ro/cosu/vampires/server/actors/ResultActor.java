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

package ro.cosu.vampires.server.actors;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.schedulers.SamplingWithReplicationScheduler;
import ro.cosu.vampires.server.schedulers.Scheduler;
import ro.cosu.vampires.server.schedulers.SimpleScheduler;
import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Computation;
import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.ExecutionInfo;
import ro.cosu.vampires.server.values.jobs.ExecutionMode;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.writers.ResultsWriter;


public class ResultActor extends AbstractActor {
    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());
    private final ZonedDateTime startTime = ZonedDateTime.now(ZoneOffset.UTC);
    private final Execution execution;

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private List<Job> results = new LinkedList<>();
    private List<ResultsWriter> writers;

    private ActorRef workActor;
    private Cancellable logSchedule;
    private Cancellable statsSchedule;
    private int totalSize = 0;

    private StatsProcessor statsProcessor = new StatsProcessor();

    ResultActor(Execution execution) {
        writers = settings.getWriters();
        this.execution = execution;
        totalSize = execution.workload().size();
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

        Scheduler scheduler = getScheduler(execution);

        workActor = getContext().actorOf(WorkActor.props(scheduler), "workActor");

        logSchedule = getContext().system().scheduler().schedule(scala.concurrent.duration.Duration.Zero(),
                scala.concurrent.duration.Duration.create(30, TimeUnit.SECONDS),
                () -> log.info("results so far: {}/{}", results.size(), totalSize), getContext().system().dispatcher());

        statsSchedule = getContext().system().scheduler().schedule(scala.concurrent.duration.Duration.Zero(),
                scala.concurrent.duration.Duration.create(500, TimeUnit.MILLISECONDS),
                () -> statsProcessor.flush(), getContext().system().dispatcher());

    }

    @Override
    public void postStop() {
        logSchedule.cancel();
        statsSchedule.cancel();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Job.class, this::handleJob)
                .match(ClientInfo.class, this::handleClientInfo)
                .match(ResourceInfo.class, this::handleResourceInfo)
                .match(ResourceControl.Shutdown.class, message -> this.handleShutdown())
                .build();
    }


    private void handleShutdown() {
        // if all jobs done then set status to finished
        if (results.size() == totalSize) {
            shutdown(ExecutionInfo.Status.FINISHED);
        } else {
            shutdown(ExecutionInfo.Status.CANCELED);
        }
    }

    private void handleResourceInfo(ResourceInfo resourceInfo) {
        statsProcessor.process(resourceInfo);
    }

    private void sendCurrentExecutionInfo(ExecutionInfo.Status status) {
        ExecutionInfo executionInfo = ExecutionInfo.empty()
                .updateTotal(totalSize)
                .updateCompleted(results.size())
                .updateStatus(status)
                .updateStats(statsProcessor.getStats())
                .updateElapsed(Duration.between(startTime, ZonedDateTime.now(ZoneOffset.UTC)).toMillis())
                .updateRemaining(totalSize - results.size());

        Execution execution = this.execution.withInfo(executionInfo);
        getContext().parent().tell(execution, getSelf());
    }

    private void handleJob(Job job) {
        workActor.forward(job, getContext());
        if (!job.computation().id().equals(Computation.BACKOFF)
                && !job.computation().id().equals(Computation.EMPTY)) {
            results.add(job);
            writers.forEach(r -> r.addResult(job));
            statsProcessor.process(job);
            sendCurrentExecutionInfo(ExecutionInfo.Status.RUNNING);
        }
        if (results.size() == totalSize) {
            // signal parent we're done
            log.debug("result actor exiting! result count: {}", results.size());
            getContext().parent().tell(ResourceControl.Shutdown.create(), getSelf());
        }
    }

    private void handleClientInfo(ClientInfo clientInfo) {
        ActorRef configActor = getContext().actorOf(ClientConfigActor.props(), "clientConfig-" + clientInfo.id());
        log.debug("got client info {}", clientInfo);
        configActor.forward(clientInfo, getContext());
        writers.forEach(r -> r.addClient(clientInfo));
        statsProcessor.process(clientInfo);
    }

    private void shutdown(ExecutionInfo.Status status) {
        log.info("Total Duration: {}", formatDuration(Duration.between(startTime, ZonedDateTime.now(ZoneOffset.UTC))));
        log.info("shutting down");
        writers.forEach(ResultsWriter::close);
        // init shutdown
        statsProcessor.flush();
        sendCurrentExecutionInfo(status);
        getContext().stop(getSelf());
    }

    private Scheduler getScheduler(Execution execution) {
        List<Job> jobs = execution.workload().jobs();
        if (execution.type().equals(ExecutionMode.SAMPLE)) {
            log.info("running in sampling mode : sampling from {} jobs", jobs.size());
            return new SamplingWithReplicationScheduler(jobs, settings.getJobDeadline(),
                    settings.getBackoffInterval(), settings.getNumberOfJobsToSample());
        } else
            return new SimpleScheduler(jobs, settings.getJobDeadline(), TimeUnit.SECONDS,
                    settings.getBackoffInterval());
    }
}
