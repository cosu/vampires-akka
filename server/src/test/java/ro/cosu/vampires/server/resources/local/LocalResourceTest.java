/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.resources.local;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

public class LocalResourceTest {
    private Injector injector;

    @Test
    public void testCreateInstanceConfig() throws Exception {
        Resource resource = getResource(Mockito.mock(DefaultExecutor.class));
        assertThat(resource.status(), equalTo(Resource.Status.SLEEPING));
    }

    @Test
    public void testStartStopLocalResource() throws Exception {
        Resource resource = getResource(Mockito.mock(DefaultExecutor.class));
        assertThat(resource.start().get().status(), is(Resource.Status.RUNNING));
        assertThat(resource.stop().get().status(), is(Resource.Status.STOPPED));
    }

    @Test
    public void testStartStopLocalResourceFail() throws Exception {
        Resource resource = getFailingResource();
        assertThat(resource.start().get().status(), is(Resource.Status.FAILED));
    }

    private Resource getFailingResource() throws IOException {

        DefaultExecutor mock = Mockito.mock(DefaultExecutor.class);
        when(mock.execute(anyObject())).thenReturn(-1);

        return getResource(mock);
    }

    private Resource getResource(DefaultExecutor mock) {
        injector = Guice.createInjector(new MockModule(mock));

        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider localProvider = rm.getProviders().get(Resource.ProviderType.LOCAL);
        Resource.Parameters parameters = localProvider.getParameters("local");

        return localProvider.create(parameters).get();
    }

    private static class MockModule extends AbstractModule {

        private Executor mock;

        MockModule(Executor mock) {
            this.mock = mock;
        }

        @Override
        protected void configure() {
            MapBinder<Resource.ProviderType, ResourceProvider> mapBinder
                    = MapBinder.newMapBinder(binder(), Resource.ProviderType.class, ResourceProvider.class);

            mapBinder.addBinding(Resource.ProviderType.LOCAL).to(LocalResourceProvider.class).asEagerSingleton();
        }

        @Provides
        private Executor provideExecutor() {
            return mock;
        }

        @Provides
        @Named("Config")
        private Config provideConfig() {
            return ConfigFactory.parseString("resources.local.local { " +
                    "command = foo," +
                    "cost=100\n" +
                    "}");
        }
    }


}
