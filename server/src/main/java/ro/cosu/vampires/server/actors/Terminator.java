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

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.LinkedList;
import java.util.List;

public class Terminator extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final List<ActorRef> refs = new LinkedList<>();


    public static Props props() {
        return Props.create(Terminator.class);
    }


    @Override
    public void onReceive(Object msg) {
        ActorRef sender = getSender();
        if (msg instanceof ResourceControl.Shutdown) {
            refs.forEach(r -> r.tell(PoisonPill.getInstance(), getSelf()));
        } else if (msg instanceof ResourceControl.Up) {
            refs.add(sender);
            getContext().watch(sender);
        } else if (msg instanceof Terminated) {
            refs.remove(sender);
            if (refs.isEmpty()) {
                log.info("shutting down system");
                getContext().system().terminate();
            } else {
                log.info("waiting for {} more", refs.size());
            }

        } else {
            unhandled(msg);
        }
    }
}
