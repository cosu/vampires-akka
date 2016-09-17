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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.workload.CreateWorkload;
import ro.cosu.vampires.server.actors.messages.workload.DeleteWorkload;
import ro.cosu.vampires.server.actors.messages.workload.QueryWorkload;
import ro.cosu.vampires.server.actors.messages.workload.ResponseWorkload;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.jobs.Workload;

public class WorkloadsActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private HashBasedTable<User, String, Workload> table = HashBasedTable.create();

    public static Props props() {
        return Props.create(WorkloadsActor.class);
    }

    private Map<String, Workload> getUserStore(User user) {
        return table.row(user);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof CreateWorkload) {
            createWorkload((CreateWorkload) message);
        } else if (message instanceof QueryWorkload) {
            sendResponse((QueryWorkload) message);
        } else if (message instanceof DeleteWorkload) {
            deleteWorkload((DeleteWorkload) message);
        } else {
            log.error("unhandled {}", message);
            unhandled(message);
        }
    }

    private void createWorkload(CreateWorkload message) {
        CreateWorkload createWorkload = message;
        Workload workload = createWorkload.workload();
        getUserStore(createWorkload.user()).put(workload.id(), workload);
        getSender().tell(workload, getSelf());
    }

    private void deleteWorkload(DeleteWorkload message) {
        Map<String, Workload> userStore = getUserStore(message.user());

        List<Workload> deleted = message.configurations().stream()
                .filter(userStore::containsKey)
                .map(userStore::remove)
                .collect(Collectors.toList());

        getSender().tell(ResponseWorkload.create(deleted), getSelf());
    }

    private void sendResponse(QueryWorkload message) {
        Map<String, Workload> userStore = getUserStore(message.user());

        List<Workload> workloads;
        if (message.resources().isEmpty())
            workloads = ImmutableList.copyOf(userStore.values());
        else
            workloads = message.resources().stream().filter(userStore::containsKey)
                    .map(userStore::get).collect(Collectors.toList());

        ResponseWorkload responseWorkload = ResponseWorkload.create(workloads);
        getSender().tell(responseWorkload, getSelf());
    }
}
