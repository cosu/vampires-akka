package ro.cosu.vampires.server.resources.local;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;

public class LocalResourceModule extends AbstractModule{

    @Override
    protected void configure() {
        MapBinder<Resource.Provider, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.Provider.class, ResourceProvider.class);

        mapbinder.addBinding(Resource.Provider.LOCAL).to(LocalResourceProvider.class).asEagerSingleton();

    }
}
