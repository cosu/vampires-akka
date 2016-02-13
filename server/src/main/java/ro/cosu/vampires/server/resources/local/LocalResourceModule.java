package ro.cosu.vampires.server.resources.local;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;

public class LocalResourceModule extends AbstractModule{

    @Override
    protected void configure() {
        MapBinder<Resource.Type, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);

        mapbinder.addBinding(Resource.Type.LOCAL).to(LocalResourceProvider.class).asEagerSingleton();
    }

    @Provides
    private Executor provideExecutor() {
        return new DefaultExecutor();
    }
}
