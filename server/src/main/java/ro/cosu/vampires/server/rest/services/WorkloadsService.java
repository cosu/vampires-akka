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

package ro.cosu.vampires.server.rest.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import akka.actor.ActorRef;
import ro.cosu.vampires.server.actors.messages.workload.CreateWorkload;
import ro.cosu.vampires.server.actors.messages.workload.DeleteWorkload;
import ro.cosu.vampires.server.actors.messages.workload.QueryWorkload;
import ro.cosu.vampires.server.actors.messages.workload.ResponseWorkload;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.jobs.WorkloadPayload;


public class WorkloadsService implements Service<Workload, WorkloadPayload> {
    private static final Logger LOG = LoggerFactory.getLogger(WorkloadsService.class);

    @Inject
    private ActorRef actorRef;


    public static TypeLiteral<Service<Workload, WorkloadPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Workload, WorkloadPayload>>() {
        };
    }

    @Override
    public List<Workload> list(User user) {
        QueryWorkload queryWorkload = QueryWorkload.all(user);
        return getWorkloads(queryWorkload);
    }

    private List<Workload> getWorkloads(QueryWorkload queryWorkload) {
        Optional<ResponseWorkload> ask = ActorUtil.ask(queryWorkload, actorRef);
        ResponseWorkload responseConfiguration = ask.orElseThrow(() -> new RuntimeException("failed to get"));
        return responseConfiguration.values();
    }

    @Override
    public Workload create(WorkloadPayload payload, User user) {

        Workload created = Workload.fromPayload(payload);

        Optional<Workload> ask = ActorUtil.ask(CreateWorkload.create(created, user), actorRef);
        LOG.debug("Created  for user {}:  {} with {} jobs", user.id(), created.id(), created.size());

        return ask.orElseThrow(() -> new RuntimeException("failed to create"));
    }

    @Override
    public Optional<Workload> delete(String id, User user) {
        DeleteWorkload deleteConfiguration = DeleteWorkload.create(Lists.newArrayList(id), user);
        Optional<ResponseWorkload> ask = ActorUtil.ask(deleteConfiguration, actorRef);
        ResponseWorkload responseConfiguration = ask.orElseThrow(() -> new RuntimeException("failed to delete"));
        List<Workload> workloads = responseConfiguration.values();
        if (workloads.isEmpty()) {
            return Optional.empty();
        } else
            return Optional.of(workloads.get(0));
    }

    @Override
    public Optional<Workload> update(WorkloadPayload payload, User user) {
        Preconditions.checkNotNull(payload, "empty payload");
        Preconditions.checkNotNull(payload.id(), "id must not be empty");

        Optional<Workload> currentOptional = get(payload.id(), user);

        if (currentOptional.isPresent()) {
            Workload workload = currentOptional.get();

            workload = workload.updateFromPayload(payload);
            CreateWorkload createConfiguration = CreateWorkload.create(workload, user);
            return ActorUtil.ask(createConfiguration, actorRef);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Workload> get(String id, User user) {
        QueryWorkload queryWorkload = QueryWorkload.create(id, user);
        List<Workload> workloads = getWorkloads(queryWorkload);
        if (workloads.isEmpty()) {
            return Optional.empty();
        } else
            return Optional.of(workloads.get(0));
    }
}
