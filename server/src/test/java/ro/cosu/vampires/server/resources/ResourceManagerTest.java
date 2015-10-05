package ro.cosu.vampires.server.resources;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ResourceManagerTest {
    @Test
    public void testDefaultLoad() throws  Exception {

        Config config = getConfig();


        Injector injector = Guice.createInjector(new ResourceModule(config));

        ResourceManager rm = injector.getInstance(ResourceManager.class);
        rm.getProviders().values().forEach(provider -> {
            Resource resource = provider.create();
            assertThat(resource.getStatus(), is(Resource.Status.CREATING));

        });
    }

    private Config getConfig() {
        Map<String, String> map = new HashMap<>();

        map.put("command", "/Users/cdumitru/Documents/workspace/java/vampires-akka/client/build/install/client/bin/client");
        map.put("user", "cdumitru");
        map.put("address", "localhost");
        map.put("privateKey", "/Users/cdumitru/.ssh/id_rsa");
        return ConfigFactory.parseMap(map);
    }

    private Config getDasConfig() {
        Map<String, String> map = new HashMap<>();

        map.put("command", "/Users/cdumitru/Documents/workspace/java/vampires-akka/client/build/install/client/bin/client");
        map.put("user", "cosmin");
        map.put("address", "fs2.das5.science.uva.nl");
        map.put("privateKey", "/Users/cosmin/.ssh/id_rsa-cosu");
        return ConfigFactory.parseMap(map);
    }

    @Test
    public void testCreateResourceDescription() throws  Exception {
        ResourceDescription test = ResourceDescription.create("test", Resource.Type.LOCAL);
        assertThat(test.id(), is(equalTo("test")));
        assertThat(test.type(), is(equalTo(Resource.Type.LOCAL)));
    }

    @Test
    public void testCreateLocalResource() throws  Exception  {


        Injector injector = Guice.createInjector(new ResourceModule(getConfig()));
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider localProvider = rm.getProviders().get(Resource.Type.LOCAL);
        Resource resource = localProvider.create();
        try {
            assertThat(resource.start().get().getStatus(), is(Resource.Status.RUNNING));
            assertThat(resource.stop().get().getStatus(), is(Resource.Status.STOPPED));
        } catch (InterruptedException | ExecutionException e1) {
            //
        }
    }

    @Test
    public void testCreateSSHResource() throws  Exception  {

        Injector injector = Guice.createInjector(new ResourceModule(getConfig()));
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider localProvider = rm.getProviders().get(Resource.Type.SSH);
        Resource resource = localProvider.create();
        try {
            assertThat(resource.start().get().getStatus(), is(Resource.Status.RUNNING));
            assertThat(resource.stop().get().getStatus(), is(Resource.Status.STOPPED));
        } catch (InterruptedException | ExecutionException e1) {
            //
        }
    }

    @Test
    public void testCreateDAS5Resource() throws  Exception  {

        Injector injector = Guice.createInjector(new ResourceModule(getDasConfig()));
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider localProvider = rm.getProviders().get(Resource.Type.DAS5);
        Resource resource = localProvider.create();
        try {
            assertThat(resource.start().get().getStatus(), is(Resource.Status.RUNNING));
            Thread.sleep(2000);
            assertThat(resource.stop().get().getStatus(), is(Resource.Status.STOPPED));
        } catch (InterruptedException | ExecutionException e1) {
            //
        }
    }

}
