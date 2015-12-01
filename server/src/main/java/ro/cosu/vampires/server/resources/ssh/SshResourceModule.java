package ro.cosu.vampires.server.resources.ssh;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.util.Ssh;

public class SshResourceModule extends AbstractModule {

    @Override
    protected void configure() {
        MapBinder<Resource.Provider, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.Provider.class, ResourceProvider.class);

        mapbinder.addBinding(Resource.Provider.SSH).to(SshResourceProvider.class).asEagerSingleton();
    }
    @Provides
    @Named("Ssh")
    Ssh provideSsh (){
        return new Ssh();
    }
}
