package ro.cosu.vampires.server.resources.das5;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.jcraft.jsch.JSchException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.util.SshClient;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


public class Das5ResourceTest {
    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new AbstractModule() {
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
            SshClient provideSsh () throws IOException, JSchException {
                SshClient sshClientMock = Mockito.mock(SshClient.class);
                when(sshClientMock.runCommand(anyString(), anyString(), anyString(), anyString(), anyInt()))
                        .thenReturn("10 10 10 10 ");
                return sshClientMock;
            }
        });

    }

    @Test
    public void createDas5Resource() throws Exception {

        Resource resource = getResource();

        assertThat(resource.status(), equalTo(Resource.Status.SLEEPING));

    }

    private Resource getResource() {
        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider das5Provider = rm.getProviders().get(Resource.Type.DAS5);
        Resource.Parameters parameters = das5Provider.getParameters("local");

        return das5Provider.create(parameters).get();
    }

    @Test
    public void testStartStopDAS5Resource () throws Exception {
        Resource resource = getResource();
        assertThat(resource.start().get().status(),  is(Resource.Status.RUNNING));
        assertThat(resource.stop().get().status(),  is(Resource.Status.STOPPED));
    }

}