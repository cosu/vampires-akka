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

import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.actors.resource.ResourceManagerActor;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.Job;

public class ExecutionActor extends UntypedActor {

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

        resourceManagerActor = getContext().actorOf(ResourceManagerActor.props(), "resourceActor");
        resourceManagerActor.tell(execution, getSelf());
        resultActor = getContext().actorOf(ResultActor.props(execution), "resultActor");
        getContext().watch(resourceManagerActor);
        getContext().watch(resultActor);

        watchees.add(resourceManagerActor);
        watchees.add(resultActor);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        // forward traffic to all actors
        if (message instanceof ClientInfo) {
            watchees.stream().forEach(actorRef -> actorRef.forward(message, getContext()));
        } else if (message instanceof Job) {
            resultActor.tell(message, getSender());
        } else if (message instanceof ResourceControl.Shutdown) {
            resourceManagerActor.forward(message, getContext());
            resultActor.forward(message, getContext());

        } else if (message instanceof Execution) {
            // send exec info back to parent
            getContext().parent().forward(message, getContext());
        } else if (message instanceof Terminated) {
            if (getSender().equals(resultActor)) {
                getContext().stop(getSelf());
            }
        } else {
            unhandled(message);
        }
    }
}
