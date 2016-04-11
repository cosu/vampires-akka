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
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;

import java.io.IOException;

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

        ResourceProvider localProvider = rm.getProviders().get(Resource.Type.LOCAL);
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
            MapBinder<Resource.Type, ResourceProvider> mapBinder
                    = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);

            mapBinder.addBinding(Resource.Type.LOCAL).to(LocalResourceProvider.class).asEagerSingleton();
        }

        @Provides
        private Executor provideExecutor() {
            return mock;
        }

        @Provides
        @Named("Config")
        private Config provideConfig() {
            return ConfigFactory.parseString("resources.local.local { " +
                    "command = foo}");
        }
    }


}
