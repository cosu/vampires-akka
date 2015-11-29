package ro.cosu.vampires.server.resources;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.typesafe.config.ConfigFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import ro.cosu.vampires.server.resources.das5.Das5ResourceParameters;
import ro.cosu.vampires.server.util.Ssh;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ResourceManagerIT {



    private Das5ResourceParameters getDasConfig() {
        return Das5ResourceParameters.builder()
                .command("vampires.sh")
                .user("cosmin")
                .address("localhost")
                .privateKey("~/.ssh/id_rsa")
                .port(2222)
                .build();
    }

    @Test
    public void testCreateResourceDescription() throws  Exception {
        ResourceDescription test = ResourceDescription.create("test", Resource.Type.LOCAL);
        assertThat(test.id(), is(equalTo("test")));
        assertThat(test.type(), is(equalTo(Resource.Type.LOCAL)));
    }

    @Test
    @Ignore
    public void testCreateLocalResource() throws  Exception  {

        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.load().getConfig("vampires")));

        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider localProvider = rm.getProviders().get(Resource.Type.LOCAL);
        Resource.Parameters parameters = localProvider.getParameters("local");
        Resource resource = localProvider.create(parameters).get();

        assertThat(resource.start().get(2, TimeUnit.SECONDS).status(), is(Resource.Status.RUNNING));
        assertThat(resource.stop().get(1, TimeUnit.SECONDS).status(), is(Resource.Status.STOPPED));

    }

    @Test

    public void testCreateSSHResource() throws  Exception  {
        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.load().getConfig("vampires")));
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider sshProvider = rm.getProviders().get(Resource.Type.SSH);
        Resource.Parameters parameters = sshProvider.getParameters("local");

        Resource resource = sshProvider.create(parameters).get();

        assertThat(resource.start().get(1, TimeUnit.SECONDS).status(), is(Resource.Status.RUNNING));
        assertThat(resource.stop().get(1, TimeUnit.SECONDS).status(), is(Resource.Status.STOPPED));

    }

    @Test
    @Ignore
    public void testCreateDAS5Resource() throws  Exception  {

        Ssh sshMock = mock(Ssh.class);

        Mockito.when(sshMock.runCommand(Mockito.anyString(),Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("10 10 10 10");

        class TestModule extends AbstractModule {

            @Override
            protected void configure() {
                bind(Ssh.class).toInstance(sshMock);
            }
        }

        Module override = Modules.override(new ResourceModule(ConfigFactory.empty())).with(new TestModule());
        Injector injector = Guice.createInjector(override );
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider localProvider = rm.getProviders().get(Resource.Type.DAS5);
        Resource resource = localProvider.create(getDasConfig()).get();

        assertThat(resource.start().get(2, TimeUnit.SECONDS).status(), is(Resource.Status.RUNNING));
        Thread.sleep(2000);
        assertThat(resource.stop().get(2, TimeUnit.SECONDS).status(), is(Resource.Status.STOPPED));
    }

}
