package ro.cosu.vampires.server.rest.controllers;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.rest.services.ConfigurationsService;
import ro.cosu.vampires.server.rest.services.Service;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.ResourceDemand;


public class ConfigurationsControllerTest extends AbstractControllerTest<Configuration, ConfigurationPayload> {


    protected AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(getTypeTokenService()).to(ConfigurationsService.class).asEagerSingleton();
                bind(ConfigurationsController.class).asEagerSingleton();
            }
        };
    }

    @Override
    protected TypeLiteral<Service<Configuration, ConfigurationPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Configuration, ConfigurationPayload>>() {
        };
    }

    @Override
    protected ConfigurationPayload getPayload() {
        ImmutableList<ResourceDemand> resourceDemands = ImmutableList.of(
                ResourceDemand.builder()
                        .count(1)
                        .provider(Resource.ProviderType.MOCK)
                        .type("bar")
                        .build());
        return ConfigurationPayload.create("foo", resourceDemands);
    }

    @Override
    protected String getPath() {
        return "/configurations";
    }

}