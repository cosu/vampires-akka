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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class LocalResourceTest {
    private Injector injector;

    @Before
    public void setUp() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                MapBinder<Resource.Type, ResourceProvider> mapBinder
                        = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);

                mapBinder.addBinding(Resource.Type.LOCAL).to(LocalResourceProvider.class).asEagerSingleton();
            }

            @Provides
            private Executor provideExecutor() {
                return Mockito.mock(DefaultExecutor.class);
            }

            @Provides
            @Named("Config")
            private Config provideConfig() {
                return ConfigFactory.parseString("resources.local.local { " +
                        "command = foo}");
            }

        });
    }


    @Test
    public void testCreateInstanceConfig() throws Exception {
        Resource resource = getResource();
        assertThat(resource.status(), equalTo(Resource.Status.SLEEPING));
    }

    @Test
    public void testStartStopLocalResource() throws Exception {
        Resource resource = getResource();
        assertThat(resource.start().get().status(), is(Resource.Status.RUNNING));
        assertThat(resource.stop().get().status(), is(Resource.Status.STOPPED));
    }


    private Resource getResource() {
        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider localProvider = rm.getProviders().get(Resource.Type.LOCAL);
        Resource.Parameters parameters = localProvider.getParameters("local");

        return localProvider.create(parameters).get();
    }


}
