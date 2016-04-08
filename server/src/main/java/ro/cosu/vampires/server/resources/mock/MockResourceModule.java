package ro.cosu.vampires.server.resources.mock;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;

public class MockResourceModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder<Resource.Type, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);

        mapbinder.addBinding(Resource.Type.MOCK).to(MockResourceProvider.class).asEagerSingleton();

    }
}
