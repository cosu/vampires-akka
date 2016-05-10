package ro.cosu.vampires.server.rest.services;


import com.google.common.base.Enums;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import akka.actor.ActorSystem;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.ExecutionPayload;
import ro.cosu.vampires.server.workload.Workload;

public class ExecutionsService {
    private Map<String, Execution> executionMap = Maps.newConcurrentMap();
    @Inject
    private ActorSystem actorSystem;
    @Inject
    private ConfigurationsService configurationsService;
    @Inject
    private WorkloadsService workloadsService;

    public Execution create(ExecutionPayload executionPayload) {

        Optional<Configuration> configurationOptional = configurationsService.getConfiguration(executionPayload.configuration());
        Optional<Workload> workloadOptional = workloadsService.getWorkload(executionPayload.workload());


        if (configurationOptional.isPresent() && workloadOptional.isPresent()) {
            Configuration configuration = configurationOptional.get();
            Workload workload = workloadOptional.get();
            ExecutionMode mode = Enums.stringConverter(ExecutionMode.class).convert(executionPayload.type());
            Execution execution = Execution.builder().workload(workload).configuration(configuration).type(mode).build();
            executionMap.put(execution.id(), execution);
            return execution;
        }

        return null;
    }



    public Collection<Execution> getExecutions() {
        return executionMap.values();
    }


    public Optional<Execution> getExecution(String id) {
        return Optional.ofNullable(executionMap.get(id));
    }
}
