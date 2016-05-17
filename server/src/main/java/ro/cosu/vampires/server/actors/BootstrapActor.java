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


import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.Map;
import java.util.Optional;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.QueryResource;
import ro.cosu.vampires.server.actors.messages.ShutdownResource;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.rest.RestModule;
import ro.cosu.vampires.server.rest.services.ConfigurationsService;
import ro.cosu.vampires.server.rest.services.WorkloadsService;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionInfo;
import ro.cosu.vampires.server.workload.WorkloadPayload;
import spark.Spark;

public class BootstrapActor extends UntypedActor {

    private final ActorRef terminator;
    private final SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private Map<String, ActorRef> executionsToActors = Maps.newHashMap();
    private Map<String, Execution> resultsMap = Maps.newHashMap();


    private RestModule restModule = new RestModule(getSelf(), settings.getProviders());
    private Injector injector;


    BootstrapActor(ActorRef terminator) {
        this.terminator = terminator;
    }

    public static Props props(ActorRef terminator) {
        return Props.create(BootstrapActor.class, terminator);
    }

    @Override
    public void preStart() {
        terminator.tell(ResourceControl.Up.create(), getSelf());
        startWebserver();
        loadFromConfig();
    }

    private void startWebserver() {
        Spark.port(settings.vampires.getInt("rest-port"));
        Spark.init();
        injector = Guice.createInjector(restModule);
    }

    private void loadFromConfig() {
        if (settings.vampires.hasPath("workloads")) {
            WorkloadsService workloadsService = injector.getInstance(WorkloadsService.class);
            settings.vampires.getConfigList("workloads").stream()
                    .map(WorkloadPayload::fromConfig)
                    .forEach(workloadsService::create);
        }

        if (settings.vampires.hasPath("configurations")) {
            ConfigurationsService configurationsService = injector
                    .getInstance(ConfigurationsService.class);
            settings.vampires.getConfigList("configurations").stream()
                    .map(ConfigurationPayload::fromConfig)
                    .forEach(configurationsService::create);
        }
    }

    private void startExecution(Execution execution) {
        ActorRef executionActor = getContext().actorOf(ExecutionActor.props(execution), execution.id());
        executionsToActors.put(execution.id(), executionActor);
        executionActor.tell(execution, getSelf());
        getContext().watch(executionActor);
        resultsMap.put(execution.id(), execution);
    }

    @Override
    public void postStop() {
        Spark.stop();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Execution) {
            Execution execution = (Execution) message;
            handleExecution(execution);
        } else if (message instanceof QueryResource) {
            QueryResource info = (QueryResource) message;
            queryResource(info);
        } else if (message instanceof ShutdownResource) {
            ShutdownResource shutdownResource = (ShutdownResource) message;
            handleShutdown(shutdownResource);
        } else if (message instanceof Terminated) {
            handleTerminated();
        } else {
            unhandled(message);
        }
    }

    private void handleShutdown(ShutdownResource shutdownResource) {

        if (executionsToActors.containsKey(shutdownResource.resourceId())) {
            Execution execution = resultsMap.get(shutdownResource.resourceId());
            if (execution.info().status().equals(ExecutionInfo.Status.STARTING) ||
                    execution.info().status().equals(ExecutionInfo.Status.RUNNING)) {
                // shut down the actor
                ActorRef executionActor = executionsToActors.get(shutdownResource.resourceId());
                executionActor.tell(ResourceControl.Shutdown.create(), getSelf());

                // update the current view of the execution
                execution = execution.withInfo(execution.info()
                        .updateStatus(ExecutionInfo.Status.STOPPING));
                resultsMap.put(execution.id(), execution);
            } else {
                log.warning("shutting down a non-running execution {}", execution);
            }

            getSender().tell(execution, getSelf());

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

    private void handleExecution(Execution execution) {
        if (!executionsToActors.containsKey(execution.id()))
            startExecution(execution);
        else {
            //a finished execution
            resultsMap.put(execution.id(), execution);
        }
    }

    private void queryResource(QueryResource info) {
        if (info.equals(QueryResource.all())) {
            getSender().tell(resultsMap.values(), getSelf());
        } else {
            if (resultsMap.containsKey(info.resourceId())) {
                getSender().tell(resultsMap.get(info.resourceId()), getSelf());
            } else {
                log.error("Invalid query for execution id {}", info.resourceId());
            }
        }
    }
}
