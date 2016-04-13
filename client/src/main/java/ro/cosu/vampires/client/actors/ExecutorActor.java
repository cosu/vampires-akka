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

package ro.cosu.vampires.client.actors;

import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.extension.ExecutorsExtension;
import ro.cosu.vampires.client.extension.ExecutorsExtensionImpl;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.Result;

public class ExecutorActor extends UntypedActor {

    private final ExecutorsExtensionImpl executors = ExecutorsExtension.ExecutorsProvider.get(getContext().system());
    private final ActorSelection monitorActor;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public ExecutorActor() {
        monitorActor = getContext().actorSelection("/user/monitor");
    }

    public static Props props() {
        return Props.create(ExecutorActor.class);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Job) {
            Job job = (Job) message;
            Executor executor = executors.getExecutor();
            Result result = executor.execute(job.computation());
            log.debug("done executing job {}", job.id());
            monitorActor.tell(job.withResult(result), getSender());
        }

        getContext().stop(getSelf());
    }


}
