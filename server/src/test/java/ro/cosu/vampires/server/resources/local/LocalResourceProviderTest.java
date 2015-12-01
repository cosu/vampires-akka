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

        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.load().getConfig("vampires")));
        ResourceManager rm = injector.getInstance(ResourceManager.class);


        ResourceProvider localProvider = rm.getProviders().get(Resource.Provider.LOCAL);
        Resource.Parameters parameters = localProvider.getParameters("local");

        Resource resource = localProvider.create(parameters).get();

        assertThat(resource.status(), equalTo(Resource.Status.CREATING));

    }


}
