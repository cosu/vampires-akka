package ro.cosu.vampires.server.resources;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

public class ResourceManagerTest {
    @Test
    public void testDefaultLoad() throws  Exception {
        Injector injector = Guice.createInjector(new ResourceModule());

        ResourceManager rm = injector.getInstance(ResourceManager.class);
        rm.getResources().values().forEach(IResource::start);
    }

}
