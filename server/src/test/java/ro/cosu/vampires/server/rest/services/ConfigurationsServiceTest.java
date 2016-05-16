/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.rest.services;


import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import org.junit.Test;

import java.util.Optional;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.workload.AutoValueUtil;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.ResourceDemand;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ConfigurationsServiceTest extends AbstractServiceTest<Configuration, ConfigurationPayload> {
    @Override
    protected AbstractModule getModule() {
        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(getTypeTokenService()).to(new TypeLiteral<ConfigurationsService>() {
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

    @Override
    public void update() throws Exception {
        assertThat(instance.list().size(), is(1));
        Configuration next = instance.list().iterator().next();

        AutoValueUtil<Configuration, ConfigurationPayload.Builder>
                configurationBuilderAutoValueUtil = new AutoValueUtil<>();
        ConfigurationPayload.Builder builder = configurationBuilderAutoValueUtil
                .builderFromPayload(next, ConfigurationPayload.builder());

        ConfigurationPayload updated = builder.description("foobar").build();

        Optional<Configuration> update = instance.update(updated);

        assertThat(update.isPresent(), is(true));
        assertThat(update.get().description(), is("foobar"));
    }

    @Test(expected = NullPointerException.class)
    public void updateFail() throws Exception {
        ConfigurationPayload configurationPayload = ConfigurationPayload.builder().description("foo").build();
        Optional<Configuration> update = instance.update(configurationPayload);

    }
}
