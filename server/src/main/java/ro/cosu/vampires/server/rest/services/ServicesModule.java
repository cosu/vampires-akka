package ro.cosu.vampires.server.rest.services;

import com.google.inject.AbstractModule;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(ConfigurationsService.class).asEagerSingleton();
        bind(WorkloadsService.class).asEagerSingleton();
        bind(ExecutionsService.class).asEagerSingleton();

    }
}
