package ro.cosu.vampires.server.resources;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ResourceManagerTest {
    @Test
    public void testDefaultLoad() throws  Exception {
        Injector injector = Guice.createInjector(new ResourceModule());

        ResourceManager rm = injector.getInstance(ResourceManager.class);
        rm.getProviders().values().forEach(e -> System.out.println(e.create().getStatus()));
    }

    @Test
    public void testCreateResource() throws  Exception {
        ResourceDescription test = ResourceDescription.create("test", Resource.Type.LOCAL);
        assertThat(test.id(), is(equalTo("test")));
        assertThat(test.type(), is(equalTo(Resource.Type.LOCAL)));
    }

}
