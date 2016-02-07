package ro.cosu.vampires.server.resources.das5;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.util.Ssh;

public class Das5ResourceModule extends AbstractModule{

    @Override
    protected void configure() {
        MapBinder<Resource.Type, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);
        mapbinder.addBinding(Resource.Type.DAS5).to(Das5ResourceProvider.class).asEagerSingleton();

    }

    @Provides @Named("DASSSH")
    Ssh provideSsh (){
        return new Ssh();
    }
}
