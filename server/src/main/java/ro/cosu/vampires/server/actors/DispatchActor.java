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

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Job;

public class DispatchActor extends UntypedActor {

    private final ActorRef workActor;


    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public DispatchActor(ActorRef workActor) {
        this.workActor = workActor;
    }

    public static Props props(ActorRef workActor) {
        return Props.create(DispatchActor.class, workActor);
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Job) {
            workActor.forward(message, getContext());
        } else if (message instanceof ClientInfo) {
            ActorRef configActor = getContext().actorOf(ConfigActor.props());
            log.debug("got client info {}", message);
            configActor.forward(message, getContext());
        } else {
            log.error("Unhandled  request from {} , {}", getSender().toString(), message);
            unhandled(message);
        }

    }
}
