package ro.cosu.vampires.server.rest.services;


import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import ro.cosu.vampires.server.actors.messages.BootstrapResource;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionPayload;

public class ExecutionsService {
    Map<String, Execution> executionMap = Maps.newConcurrentMap();
    @Inject
    private ActorSystem actorSystem;
    @Inject
    private ConfigurationsService configurationsService;
    @Inject
    private WorkloadsService workloadsService;

    public Execution create(ExecutionPayload executionPayload) {
//        System.out.println(executionPayload);
//        Execution execution= Execution.fromPayload(executionPayload);
        // TODO: 6-5-16 pass this to the actor system
//        executionMap.put(execution.id(), execution);
//
//        Optional<Configuration> configurationOptional = configurationsService.getConfiguration(execution.configuration());
//        Optional<Workload> workloadOptional = workloadsService.getWorkload(execution.workload());
//
//        Configuration configuration = configurationOptional.get();
//        Workload workload = workloadOptional.get();
//
//
//        if (execution.type().toLowerCase().equals("sampling")) {
//            List<ResourceDemand> resourceList = configuration.resources()
//                    .stream().map(r -> r.withCount(1)).collect(Collectors.toList());
//
//            configuration = configuration.withResources(ImmutableList.copyOf(resourceList));
//
//        }
//        startConfiguration(configuration);

        return null;
    }


    private void startConfiguration(Configuration configuration) {

        ActorSelection actorSelection = actorSystem.actorSelection("/ResourceActor");
        configuration.resources().stream().forEach(r -> {

            Resource.ProviderType provider = r.provider();


            IntStream.rangeClosed(1, r.count()).forEach(i -> actorSelection.tell(BootstrapResource.create(provider, r.type()), null));

        });
    }

    public Collection<Execution> getExecutions() {
        return executionMap.values();
    }


    public Optional<Execution> getExecution(String id) {
        return Optional.ofNullable(executionMap.get(id));
    }
}
