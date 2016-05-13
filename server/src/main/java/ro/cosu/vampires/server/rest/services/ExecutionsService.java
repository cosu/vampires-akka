package ro.cosu.vampires.server.rest.services;


import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import akka.actor.ActorRef;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionPayload;
import ro.cosu.vampires.server.workload.Workload;

public class ExecutionsService implements Service<Execution, ExecutionPayload> {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionsService.class);

    private Map<String, Execution> executionMap = Collections.synchronizedSortedMap(Maps.newTreeMap());

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
    public Collection<Execution> list() {
        return executionMap.values();
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
                .build();
        startExecution(execution);
        return execution;
    }

    private void startExecution(Execution execution) {
        executionMap.put(execution.id(), execution);
        LOG.info("starting execution: {}", execution);
        actorRef.tell(execution, ActorRef.noSender());
    }

    @Override
    public Optional<Execution> delete(String id) {
        throw new IllegalArgumentException("delete not implemented");
    }

    @Override
    public Optional<Execution> update(Execution updated) {
        throw new IllegalArgumentException("update not implemented");
    }

    @Override
    public Optional<Execution> get(String id) {
        return Optional.ofNullable(executionMap.get(id));
    }
}
