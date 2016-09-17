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
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import akka.actor.ActorRef;
import ro.cosu.vampires.server.actors.messages.execution.QueryExecution;
import ro.cosu.vampires.server.actors.messages.execution.ResponseExecution;
import ro.cosu.vampires.server.actors.messages.execution.StartExecution;
import ro.cosu.vampires.server.actors.messages.resource.DeleteExecution;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.ExecutionInfo;
import ro.cosu.vampires.server.values.jobs.ExecutionPayload;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.resources.Configuration;

public class ExecutionsService implements Service<Execution, ExecutionPayload> {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionsService.class);

    @Inject
    private ConfigurationsService configurationsService;

    @Inject
    private WorkloadsService wService;

    @Inject
    private ActorRef actorRef;

    public static TypeLiteral<Service<Execution, ExecutionPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Execution, ExecutionPayload>>() {
        };
    }

    @Override
    public List<Execution> list(User user) {
        return getExecutions(QueryExecution.all(user));
    }

    private List<Execution> getExecutions(QueryExecution queryConfiguration) {
        Optional<ResponseExecution> ask = ActorUtil.ask(queryConfiguration, actorRef);
        ResponseExecution responseExecution = ask.orElseThrow(() -> new RuntimeException("timed out on get"));
        return responseExecution.values();
    }

    @Override
    public Execution create(ExecutionPayload executionPayload, User user) {

        Configuration configuration = configurationsService.get(executionPayload.configuration(), user).orElseThrow(() ->
                new IllegalArgumentException("could not find configuration with id " + executionPayload.configuration()));

        Workload workload = wService.get(executionPayload.workload(), user).orElseThrow(()
                -> new IllegalArgumentException("could not find workload with id " + executionPayload.workload()));

        Execution execution = Execution.builder().workload(workload)
                .configuration(configuration)
                .type(executionPayload.type())
                .info(ExecutionInfo.empty().updateRemaining(workload.size()))
                .build();
        LOG.info("user {} starting execution: {}", user, execution.id());
        LOG.debug("Execution:\n{}", execution);

        actorRef.tell(StartExecution.create(user, execution), ActorRef.noSender());
        return execution;
    }


    @Override
    public Optional<Execution> delete(String id, User user) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "id must not be empty");

        Optional<ResponseExecution> ask = ActorUtil.ask(DeleteExecution.create(id, user), actorRef);
        ResponseExecution responseExecution = ask.orElseThrow(() -> new RuntimeException("failed to delete"));
        List<Execution> configurations = responseExecution.values();
        if (configurations.isEmpty()) {
            return Optional.empty();
        } else
            return Optional.of(configurations.get(0));

    }

    @Override
    public Optional<Execution> update(ExecutionPayload updated, User user) {
        throw new IllegalArgumentException("not implemented");
    }


    @Override
    public Optional<Execution> get(String id, User user) {
        List<Execution> executions = getExecutions(QueryExecution.create(id, user));
        if (executions.isEmpty()) {
            return Optional.empty();
        } else
            return Optional.of(executions.get(0));
    }
}
