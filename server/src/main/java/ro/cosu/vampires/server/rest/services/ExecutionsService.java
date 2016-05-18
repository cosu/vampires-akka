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
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import ro.cosu.vampires.server.actors.messages.QueryResource;
import ro.cosu.vampires.server.actors.messages.ShutdownResource;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionInfo;
import ro.cosu.vampires.server.workload.ExecutionPayload;
import ro.cosu.vampires.server.workload.Workload;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

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

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Execution> list() {

        Collection<Execution> executions = Lists.newArrayList();

        Timeout timeout = new Timeout(Duration.create(100, "milliseconds"));

        Future<Object> ask = Patterns.ask(actorRef, QueryResource.all(), timeout);

        try {
            Object result = Await.result(ask, timeout.duration());
            if (result instanceof Collection) {
                // silly java types
                executions = (Collection<Execution>) result;
            }
            return executions;
        } catch (Exception e) {
            LOG.error("Failed to get execution {}", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Execution create(ExecutionPayload executionPayload) {

        LOG.debug("{} {}", configurationsService, wService);
        Configuration configuration = configurationsService.get(executionPayload.configuration()).orElseThrow(() ->
                new IllegalArgumentException("could not find configuration with id " + executionPayload.configuration()));

        Workload workload = wService.get(executionPayload.workload()).orElseThrow(()
                -> new IllegalArgumentException("could not find workload with id " + executionPayload.workload()));

        Execution execution = Execution.builder().workload(workload)
                .configuration(configuration)
                .type(executionPayload.type())
                .info(ExecutionInfo.empty().updateRemaining(workload.size()))
                .build();
        startExecution(execution);
        return execution;
    }

    private void startExecution(Execution execution) {
        LOG.info("starting execution: {}", execution);
        actorRef.tell(execution, ActorRef.noSender());
    }

    @Override
    public Optional<Execution> delete(String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "id must not be empty");
        return sendMessage(ShutdownResource.withId(id));
    }

    @Override
    public Optional<Execution> update(ExecutionPayload updated) {
        throw new IllegalArgumentException("not implemented");
    }

    private Optional<Execution> sendMessage(Object message) {
        Timeout timeout = new Timeout(Duration.create(100, "milliseconds"));

        Future<Object> ask = Patterns.ask(actorRef, message, timeout);
        try {
            Execution execution = (Execution) Await.result(ask, timeout.duration());
            return Optional.ofNullable(execution);
        } catch (Exception e) {
            LOG.error("Failed to get execution {}", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Execution> get(String id) {
        return sendMessage(QueryResource.withId(id));
    }



}
