package ro.cosu.vampires.server.resources.ssh;

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


public class SshResourceTest {
    @Test
    public void createSshResource() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                MapBinder<Resource.Type, ResourceProvider> mapbinder
                        = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);

                mapbinder.addBinding(Resource.Type.SSH).to(SshResourceProvider.class).asEagerSingleton();
            }
            @Provides
            @Named("Config")
            private Config provideConfig(){
                return ConfigFactory.parseString("resources.ssh.local { " +
                        "user= foo\n" +
                        "address=bar\n" +
                        "privateKey=baz\n" +
                        "command = foo}");
            }

            @Provides @Named("Ssh")
            Ssh provideSsh (){
                return Mockito.mock(Ssh.class);
            }
        });

        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider sshProvider = rm.getProviders().get(Resource.Type.SSH);
        Resource.Parameters parameters = sshProvider.getParameters("local");

        Resource resource = sshProvider.create(parameters).get();

        assertThat(resource.status(), equalTo(Resource.Status.SLEEPING));

    }

}