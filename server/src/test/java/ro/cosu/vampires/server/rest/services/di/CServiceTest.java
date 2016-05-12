package ro.cosu.vampires.server.rest.services.di;


import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.ResourceDemand;

public class CServiceTest extends AbstractDiTest<Configuration, ConfigurationPayload> {
    @Override
    protected AbstractModule getModule() {
        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(getTypeTokenService()).to(new TypeLiteral<CService>() {
                });
            }
        };
        return module;
    }

    @Override
    protected TypeLiteral<Service<Configuration, ConfigurationPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Configuration, ConfigurationPayload>>() {
        };
    }

    @Override
    protected ConfigurationPayload getPayload() {

        ImmutableList<ResourceDemand> resourceDemands = ImmutableList.of(ResourceDemand.builder().count(1)
                .provider(Resource.ProviderType.MOCK).type("bar").build());
        return ConfigurationPayload.create("foo", resourceDemands);
    }
}
