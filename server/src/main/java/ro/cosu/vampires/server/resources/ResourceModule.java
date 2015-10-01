package ro.cosu.vampires.server.resources;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import ro.cosu.vampires.server.resources.local.LocalResource;
import ro.cosu.vampires.server.resources.ssh.SshResource;

public class ResourceModule extends AbstractModule{
    @Override
    protected void configure() {
        MapBinder<String, IResource> mapbinder
                = MapBinder.newMapBinder(binder(), String.class, IResource.class);
        mapbinder.addBinding("ssh").to(SshResource.class);
        mapbinder.addBinding("local").to(LocalResource.class);

    }

}
