package ro.cosu.vampires.server.rest.services;


import com.google.common.collect.Maps;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionPayload;
import ro.cosu.vampires.server.workload.Workload;

public class ExecutionsService {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionsService.class);

    private Map<String, Execution> executionMap = Maps.newConcurrentMap();
    @Inject
    private ActorSystem actorSystem;
    @Inject
    private ConfigurationsService configurationsService;
    @Inject
    private WorkloadsService workloadsService;

    public Optional<Execution> create(ExecutionPayload executionPayload) {
        Execution execution = null;
        Optional<Configuration> configurationOptional = configurationsService.getConfiguration(executionPayload.configuration());
        Optional<Workload> workloadOptional = workloadsService.getWorkload(executionPayload.workload());


        if (configurationOptional.isPresent() && workloadOptional.isPresent()) {
            Configuration configuration = configurationOptional.get();
            Workload workload = workloadOptional.get();

            execution = Execution.builder().workload(workload).configuration(configuration).type(executionPayload.type()).build();
            startExecution(execution);
        }

        return Optional.ofNullable(execution);
    }

    public void startExecution(Execution execution) {
        executionMap.put(execution.id(), execution);

        actorSystem.actorFor("/user/bootstrap").tell(execution, ActorRef.noSender());
    }


    public Collection<Execution> getExecutions() {
        return executionMap.values();
    }


    public Optional<Execution> getExecution(String id) {
        return Optional.ofNullable(executionMap.get(id));
    }
}
