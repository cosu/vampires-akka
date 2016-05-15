package ro.cosu.vampires.server.rest.services;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionPayload;
import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConfigurationsService.class).asEagerSingleton();
        bind(ExecutionsService.class).asEagerSingleton();
        bind(WorkloadsService.class).asEagerSingleton();
        bind(ProvidersService.class).asEagerSingleton();

        bind(new TypeLiteral<Service<Configuration, ConfigurationPayload>>() {
        })
                .to(new TypeLiteral<ConfigurationsService>() {
                }).asEagerSingleton();

        bind(new TypeLiteral<Service<Workload, WorkloadPayload>>() {
        })
                .to(new TypeLiteral<WorkloadsService>() {
                }).asEagerSingleton();

        bind(new TypeLiteral<Service<Execution, ExecutionPayload>>() {
        })
                .to(new TypeLiteral<ExecutionsService>() {
                }).asEagerSingleton();
    }
}
