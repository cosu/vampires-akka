package ro.cosu.vampires.server.resources.ssh;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Ignore;
import org.junit.Test;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceModule;
import ro.cosu.vampires.server.resources.ResourceProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class SshResourceTest {
    @Test
    @Ignore
    public void testSshResoure() throws Exception {
        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.load().getConfig("vampires")));
        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider sshProvider = rm.getProviders().get(Resource.Provider.SSH);
        Resource.Parameters parameters = sshProvider.getParameters("local");

        Resource resource = sshProvider.create(parameters).get();


        assertThat(resource.status(), equalTo(Resource.Status.CREATING));

    }

}
