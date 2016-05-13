package ro.cosu.vampires.server.rest.services;

import com.google.common.collect.Maps;
import com.google.inject.TypeLiteral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;


public class WorkloadsService implements Service<Workload, WorkloadPayload> {
    private static final Logger LOG = LoggerFactory.getLogger(WorkloadsService.class);

    private Map<String, Workload> workloads = Collections.synchronizedSortedMap(Maps.newTreeMap());

    public static TypeLiteral<Service<Workload, WorkloadPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Workload, WorkloadPayload>>() {
        };
    }

    @Override
    public Collection<Workload> list() {
        return workloads.values();
    }

    @Override
    public Workload create(WorkloadPayload payload) {
        LOG.debug("Creating workload {}", payload);
        Workload created = Workload.fromPayload(payload);
        workloads.put(created.id(), created);
        return created;
    }

    @Override
    public Optional<Workload> delete(String id) {
        return Optional.ofNullable(workloads.remove(id));
    }

    @Override
    public Optional<Workload> update(Workload updated) {
        if (workloads.containsKey(updated.id())) {
            Workload workload = updated.touch();
            workloads.put(updated.id(), workload);
            return Optional.of(workload);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Workload> get(String id) {
        return Optional.ofNullable(workloads.get(id));
    }
}
