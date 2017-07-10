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


import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.resource.BootstrapResource;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.actors.resource.ResourceManagerActor;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.resources.ResourceDemand;

class ExecutionActor extends AbstractActor {

    private Set<ActorRef> watchees = Sets.newLinkedHashSet();
    private ActorRef resourceManagerActor;
    private ActorRef resultActor;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public ExecutionActor(Execution execution) {
        startExecution(execution);
    }

    public static Props props(Execution execution) {
        return Props.create(ExecutionActor.class, execution);
    }

    private void startExecution(Execution execution) {

        resourceManagerActor = getContext().actorOf(ResourceManagerActor.props(), "resourceManagerActor");
        resultActor = getContext().actorOf(ResultActor.props(execution), "resultActor");
        getContext().watch(resourceManagerActor);
        getContext().watch(resultActor);

        watchees.add(resourceManagerActor);
        watchees.add(resultActor);

        execution.configuration().withMode(execution.type()).resources().stream()
                .flatMap(resourceDemand -> bootstrapResourceList(execution.id(), resourceDemand).stream())
                .forEach(bootstrapResource -> resourceManagerActor.tell(bootstrapResource, getSelf()));
    }

    private List<BootstrapResource> bootstrapResourceList(String executionid, ResourceDemand resourceDemand) {
        return IntStream.range(0, resourceDemand.count()).boxed()
                .map(i -> BootstrapResource.create(
                        resourceDemand.resourceDescription().provider(),
                        resourceDemand.resourceDescription().type(),
                        executionid))
                .collect(Collectors.toList());
    }

    private void handleTerminated() {
        if (getSender().equals(resultActor)) {
            getContext().stop(getSelf());
        }
        if (getSender().equals(resourceManagerActor)) {
            resultActor.tell(ResourceControl.Shutdown.create(), getContext().parent());
        }
        watchees.remove(getSender());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClientInfo.class, message-> watchees.forEach(actorRef -> actorRef.forward(message, getContext())))
                .match(Job.class, message-> resultActor.tell(message, getSender()))
                .match(ResourceControl.Shutdown.class , message -> resourceManagerActor.forward(message, getContext()))
                .match(ResourceInfo.class, message -> resultActor.forward(message, getContext()))
                .match(Execution.class, message -> getContext().parent().forward(message, getContext()))
                .match(Terminated.class, message-> handleTerminated())
                .build();
    }
}
