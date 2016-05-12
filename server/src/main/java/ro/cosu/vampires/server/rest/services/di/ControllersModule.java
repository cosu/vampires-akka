package ro.cosu.vampires.server.rest.services.di;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;

public class ControllersModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(new TypeLiteral<Service<Configuration, ConfigurationPayload>>() {
        })
                .to(new TypeLiteral<CService>() {
                });

        bind(new TypeLiteral<Service<Workload, WorkloadPayload>>() {
        })
                .to(new TypeLiteral<WService>() {
                });

        bind(CService.class).asEagerSingleton();
        bind(WService.class).asEagerSingleton();

    }
}
