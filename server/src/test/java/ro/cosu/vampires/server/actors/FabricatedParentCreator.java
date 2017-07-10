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

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedAbstractActor;
import akka.japi.Creator;
import akka.testkit.TestProbe;

class FabricatedParentCreator implements Creator<Actor> {
    private final TestProbe proxy;
    private Props props;

    public FabricatedParentCreator(TestProbe proxy, Props props) {
        this.proxy = proxy;
        this.props = props;
    }

    @Override
    public Actor create() throws Exception {
        return new UntypedAbstractActor() {
            final ActorRef child = context().actorOf(props, "child");

            @Override
            public void preStart() {
                context().watch(child);
            }

            @Override
            public void onReceive(Object x) throws Exception {
                if (sender().equals(child)) {
                    if (x instanceof Terminated) {
                        context().stop(getSelf());
                    } else
                        proxy.ref().forward(x, context());
                } else {
                    child.forward(x, context());
                }

            }
        };
    }
}