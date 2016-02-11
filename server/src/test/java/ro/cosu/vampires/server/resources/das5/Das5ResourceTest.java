package ro.cosu.vampires.server.resources.das5;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.mockito.Mockito;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.util.Ssh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created on 11-2-16.
 */
public class Das5ResourceTest {
    @Test
    public void createDas5Resource() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                MapBinder<Resource.Type, ResourceProvider> mapbinder
                        = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);

                mapbinder.addBinding(Resource.Type.DAS5).to(Das5ResourceProvider.class).asEagerSingleton();
            }
            @Provides
            @Named("Config")
            private Config provideConfig(){
                return ConfigFactory.parseString("resources.das5.local { " +
                        "user= foo\n" +
                        "address=bar\n" +
                        "privateKey=baz\n" +
                        "command = foo}");
            }

            @Provides @Named("DASSSH")
            Ssh provideSsh (){
                return Mockito.mock(Ssh.class);
            }
        });

        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider das5Provider = rm.getProviders().get(Resource.Type.DAS5);
        Resource.Parameters parameters = das5Provider.getParameters("local");

        Resource resource = das5Provider.create(parameters).get();

        assertThat(resource.status(), equalTo(Resource.Status.SLEEPING));

    }

}