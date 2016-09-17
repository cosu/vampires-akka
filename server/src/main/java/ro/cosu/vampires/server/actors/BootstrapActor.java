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


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.configuration.ConfigurationMessage;
import ro.cosu.vampires.server.actors.messages.execution.QueryExecution;
import ro.cosu.vampires.server.actors.messages.execution.ResponseExecution;
import ro.cosu.vampires.server.actors.messages.execution.StartExecution;
import ro.cosu.vampires.server.actors.messages.resource.DeleteExecution;
import ro.cosu.vampires.server.actors.messages.workload.WorkloadMessage;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.rest.RestModule;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.ExecutionInfo;
import spark.Spark;

public class BootstrapActor extends UntypedActor {

    private final ActorRef terminator;
    private final ActorRef configurationsActor;
    private final ActorRef workloadsActor;
    private final SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private Map<String, ActorRef> executionsToActors = Maps.newHashMap();
    private HashBasedTable<User, String, Execution> executionHashBasedTable = HashBasedTable.create();


    private RestModule restModule = new RestModule(getSelf(), settings.getProviders(), settings.vampires.getConfig("rest"));


    BootstrapActor(ActorRef terminator) {
        this.terminator = terminator;
        configurationsActor = getContext().actorOf(ConfigurationsActor.props(), "configurations");
        workloadsActor = getContext().actorOf(WorkloadsActor.props(), "workloads");
    }

    public static Props props(ActorRef terminator) {
        return Props.create(BootstrapActor.class, terminator);
    }

    @Override
    public void preStart() {
        terminator.tell(ResourceControl.Up.create(), getSelf());
        startWebserver();
    }

    private void startWebserver() {
        Spark.port(settings.vampires.getInt("rest.port"));
        Spark.init();
        Guice.createInjector(restModule);
    }


    @Override
    public void postStop() {
        Spark.stop();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartExecution) {
            StartExecution startExecution = (StartExecution) message;
            handleStartExecution(startExecution);
        } else if (message instanceof Execution) {
            Execution execution = (Execution) message;
            handleExecution(execution);
        } else if (message instanceof QueryExecution) {
            QueryExecution queryExecution = (QueryExecution) message;
            queryExecution(queryExecution);
        } else if (message instanceof DeleteExecution) {
            DeleteExecution shutdownResource = (DeleteExecution) message;
            handleShutdown(shutdownResource);
        } else if (message instanceof ConfigurationMessage) {
            configurationsActor.forward(message, getContext());
        } else if (message instanceof WorkloadMessage) {
            workloadsActor.forward(message, getContext());
        } else if (message instanceof Terminated) {
            handleTerminated();
        } else {
            unhandled(message);
        }
    }


    private void handleShutdown(DeleteExecution deleteExecution) {
        User user = deleteExecution.user();

        boolean userHasExecution = getResultsMap(user).containsKey(deleteExecution.resourceId());
        boolean executionHasActor = executionsToActors.containsKey(deleteExecution.resourceId());

        if (userHasExecution && executionHasActor) {
            Execution execution = getResultsMap(user).get(deleteExecution.resourceId());
            boolean executionCanShutdown = ExecutionInfo.isActiveStatus(execution.info().status());
            if (executionCanShutdown) {
                ActorRef executionActor = executionsToActors.get(deleteExecution.resourceId());
                executionActor.tell(ResourceControl.Shutdown.create(), getSelf());

                // update the current view of the execution
                execution = execution.withInfo(execution.info()
                        .updateStatus(ExecutionInfo.Status.STOPPING));
                getResultsMap(user).put(execution.id(), execution);
            } else {
                log.warning("shutting down a non-running execution {}", execution);
            }
            getSender().tell(ResponseExecution.create(ImmutableList.of(execution)), getSelf());
        }
    }

    private void handleTerminated() {
        Optional<String> execId =
                executionsToActors.entrySet().stream()
                        .filter(e -> e.getValue().equals(getSender()))
                        .map(Map.Entry::getKey).findFirst();

        if (execId.isPresent()) {
            String id = execId.get();
            executionsToActors.remove(id);
            log.info("{} terminated ", id);
        }
    }

    private void handleStartExecution(StartExecution startExecution) {
        Execution execution = startExecution.execution();

        if (!executionsToActors.containsKey(execution.id())) {
            ActorRef executionActor = getContext().actorOf(ExecutionActor.props(execution), execution.id());
            executionsToActors.put(execution.id(), executionActor);
            executionActor.tell(execution, getSelf());
            getContext().watch(executionActor);
            getResultsMap(startExecution.user()).put(execution.id(), execution);
        }
    }

    private void handleExecution(Execution execution) {
        //lsupdate or finished execution
        User user = executionHashBasedTable.column(execution.id()).keySet().iterator().next();
        getResultsMap(user).put(execution.id(), execution);
    }

    private Map<String, Execution> getResultsMap(User user) {
        return executionHashBasedTable.row(user);
    }


    private void queryExecution(QueryExecution info) {
        User user = info.user();
        List<Execution> executionList;
        if (info.equals(QueryExecution.all(info.user()))) {
            executionList = new ArrayList<>(getResultsMap(user).values());
        } else {
            executionList = Optional.ofNullable(getResultsMap(user).get(info.resourceId()))
                    .map(Lists::newArrayList)
                    .orElse(Lists.newArrayList());
        }
        getSender().tell(ResponseExecution.create(executionList), getSelf());

    }
}
