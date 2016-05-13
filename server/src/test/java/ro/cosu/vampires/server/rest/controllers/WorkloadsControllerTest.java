package ro.cosu.vampires.server.rest.controllers;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

import ro.cosu.vampires.server.rest.services.Service;
import ro.cosu.vampires.server.rest.services.WorkloadsService;
import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;


public class WorkloadsControllerTest extends AbstractControllerTest<Workload, WorkloadPayload> {
    @Override
    protected TypeLiteral<Service<Workload, WorkloadPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Workload, WorkloadPayload>>() {
        };
    }

    @Override
    protected Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(getTypeTokenService()).to(WorkloadsService.class).asEagerSingleton();
                bind(WorkloadsController.class).asEagerSingleton();
            }
        };
    }

    @Override
    protected WorkloadPayload getPayload() {
        return WorkloadPayload.builder().sequenceStart(0)
                .url("foo")
                .sequenceStop(10).task("foo").format("%d").build();
    }

    @Override
    protected String getPath() {
        return "/workloads";
    }
}
