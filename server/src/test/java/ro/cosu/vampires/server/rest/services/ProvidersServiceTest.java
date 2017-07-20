package ro.cosu.vampires.server.rest.services;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.values.resources.ProviderDescription;
import ro.cosu.vampires.server.values.resources.ResourceDescription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ProvidersServiceTest {

    public Injector getInjector() throws Exception {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }
            @Provides
            private Map<Resource.ProviderType, ProviderDescription> getProviders() {
                HashMap<Resource.ProviderType, ProviderDescription> providers = Maps.newHashMap();

                ResourceDescription resourceDescription = ResourceDescription.builder()
                        .provider(Resource.ProviderType.MOCK).cost(0.).resourceType("mock").build();

                Map<String, ResourceDescription> resourceDescriptionMap = Maps.newHashMap();
                resourceDescriptionMap.put("mock", resourceDescription);

                ProviderDescription mock = ProviderDescription.builder()
                        .resourceDescriptions(ImmutableMap.copyOf(resourceDescriptionMap))
                        .provider(Resource.ProviderType.MOCK).description("mock").build();
                providers.put(Resource.ProviderType.MOCK, mock);
                return  providers;
            }

        });
    }

    @Test
    public void list() throws Exception {

        ProvidersService instance = getInjector().getInstance(ProvidersService.class);

        Collection<ProviderDescription> listResult = instance.list();
        assertThat(listResult.size(), is(1));
        assertThat(listResult.iterator().next().provider(), is(Resource.ProviderType.MOCK));
    }
}