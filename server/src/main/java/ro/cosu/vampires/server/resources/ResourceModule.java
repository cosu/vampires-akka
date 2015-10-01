package ro.cosu.vampires.server.resources;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import ro.cosu.vampires.server.resources.local.LocalResourceProvider;
import ro.cosu.vampires.server.resources.ssh.SshResourceProvider;

public class ResourceModule extends AbstractModule{
    @Override
    protected void configure() {
        MapBinder<String, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), String.class, ResourceProvider.class);
        mapbinder.addBinding("ssh").to(SshResourceProvider.class);
        mapbinder.addBinding("local").to(LocalResourceProvider.class);

    }

}
