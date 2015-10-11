package ro.cosu.vampires.server.resources;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.server.resources.das5.Das5ResourceParameters;
import ro.cosu.vampires.server.resources.local.LocalResourceParameters;
import ro.cosu.vampires.server.resources.ssh.SshResourceParameters;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ResourceManagerTest {

    private SshResourceParameters getSshConfig() {

        return SshResourceParameters.builder()
                .command("/Users/cdumitru/Documents/workspace/java/vampires-akka/client/build/install/client/bin" +
                        "/client")
                .user("cdumitru")
                .address("localhost")
                .privateKey("/Users/cdumitru/.ssh/id_rsa")
                .build();

    }

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
    public void testCreateLocalResource() throws  Exception  {


        LocalResourceParameters parameters = LocalResourceParameters.builder().command("sleep 5").build();

        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.empty()));
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider localProvider = rm.getProviders().get(Resource.Type.LOCAL);
        Resource resource = localProvider.create(parameters);

        assertThat(resource.start().get(1, TimeUnit.SECONDS).getStatus(), is(Resource.Status.RUNNING));
        assertThat(resource.stop().get(1, TimeUnit.SECONDS).getStatus(), is(Resource.Status.STOPPED));

    }

    @Test
    public void testCreateSSHResource() throws  Exception  {

        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.empty()));
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider sshProvider = rm.getProviders().get(Resource.Type.SSH);
        Resource resource = sshProvider.create(getSshConfig());

        assertThat(resource.start().get(1, TimeUnit.SECONDS).getStatus(), is(Resource.Status.RUNNING));
        assertThat(resource.stop().get(1, TimeUnit.SECONDS).getStatus(), is(Resource.Status.STOPPED));

    }

    @Test
    public void testCreateDAS5Resource() throws  Exception  {

        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.empty()));
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider localProvider = rm.getProviders().get(Resource.Type.DAS5);
        Resource resource = localProvider.create(getDasConfig());

        assertThat(resource.start().get(2, TimeUnit.SECONDS).getStatus(), is(Resource.Status.RUNNING));
        Thread.sleep(2000);
        assertThat(resource.stop().get(2, TimeUnit.SECONDS).getStatus(), is(Resource.Status.STOPPED));
    }

}
