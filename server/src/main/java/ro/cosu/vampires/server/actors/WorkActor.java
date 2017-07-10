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

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.schedulers.Scheduler;
import ro.cosu.vampires.server.values.jobs.Computation;
import ro.cosu.vampires.server.values.jobs.Job;

public class WorkActor extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private Scheduler scheduler;

    WorkActor(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static Props props(Scheduler scheduler) {
        return Props.create(WorkActor.class, scheduler);
    }

    private void receiveJob(Job receivedJob) {
        if (!receivedJob.computation().id().equals(Computation.BACKOFF)
                && !receivedJob.computation().id().equals(Computation.EMPTY)) {
            scheduler.markDone(receivedJob);
        }

        Job work = scheduler.getJob(receivedJob.from());
        getSender().tell(work, getSelf());

        if (scheduler.isDone()) {
            log.debug("work actor exiting");
            getContext().stop(getSelf());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Job.class , this::receiveJob)
                .matchAny(message -> {
                    log.warning("unhandled message from {}", getSender());
                    unhandled(message);
                })
                .build();
    }
}
