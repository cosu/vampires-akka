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

import java.util.LinkedList;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.resource.ResourceControl;

public class Terminator extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final List<ActorRef> refs = new LinkedList<>();


    public static Props props() {
        return Props.create(Terminator.class);
    }


    private void handleTerminated() {
        refs.remove(getSender());
        if (refs.isEmpty()) {
            log.info("shutting down system");
            getContext().system().terminate();
        } else {
            log.info("waiting for {} more", refs.size());
        }
    }

    private void handleUp() {
        ActorRef sender = getSender();
        refs.add(getSender());
        getContext().watch(sender);
        log.debug("watching {}", sender);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ResourceControl.Shutdown.class, message -> refs.forEach(r -> r.tell(PoisonPill.getInstance(), getSelf())))
                .match(ResourceControl.Up.class, message -> handleUp())
                .match(Terminated.class, message -> handleTerminated())
                .build();
    }
}
