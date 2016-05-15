package ro.cosu.vampires.server.rest.controllers;


import com.google.inject.AbstractModule;

public class ControllersModule extends AbstractModule{

    @Override
    protected void configure() {
        bind(ProvidersController.class).asEagerSingleton();
        bind(ConfigurationsController.class).asEagerSingleton();
        bind(ExecutionsController.class).asEagerSingleton();
        bind(WorkloadsController.class).asEagerSingleton();
        bind(ExceptionMapper.class).asEagerSingleton();
    }
}
