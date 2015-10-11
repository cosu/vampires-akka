package ro.cosu.vampires.server.resources.local;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceModule;
import ro.cosu.vampires.server.resources.ResourceProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class LocalResourceProviderTest {

    @Test
    public void testCreateInstanceConfig() throws Exception {

        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.load()));
        ResourceManager rm = injector.getInstance(ResourceManager.class);


        ResourceProvider localProvider = rm.getProviders().get(Resource.Type.LOCAL);
        Resource resource = localProvider.create("local");



        assertThat(resource.getStatus(), equalTo(Resource.Status.CREATING));

    }
}
