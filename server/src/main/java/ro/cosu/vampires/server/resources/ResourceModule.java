package ro.cosu.vampires.server.resources;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import ro.cosu.vampires.server.resources.local.LocalResourceProvider;
import ro.cosu.vampires.server.resources.ssh.SshResourceProvider;

public class ResourceModule extends AbstractModule{
    @Override
    protected void configure() {
        MapBinder<Resource.Type, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);
        mapbinder.addBinding(Resource.Type.SSH).to(SshResourceProvider.class).asEagerSingleton();
        mapbinder.addBinding(Resource.Type.LOCAL).to(LocalResourceProvider.class).asEagerSingleton();


    }

}
