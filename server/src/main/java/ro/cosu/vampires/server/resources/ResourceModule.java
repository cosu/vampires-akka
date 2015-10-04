package ro.cosu.vampires.server.resources;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import ro.cosu.vampires.das5.Das5ResourceProvider;
import ro.cosu.vampires.server.resources.local.LocalResourceProvider;
import ro.cosu.vampires.server.resources.ssh.SshResourceProvider;

public class ResourceModule extends AbstractModule{
    private Config config;

    ResourceModule(Config config) {
        this.config = config;
    }


    @Override
    protected void configure() {
        MapBinder<Resource.Type, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);
        mapbinder.addBinding(Resource.Type.SSH).to(SshResourceProvider.class).asEagerSingleton();
        mapbinder.addBinding(Resource.Type.LOCAL).to(LocalResourceProvider.class).asEagerSingleton();
        mapbinder.addBinding(Resource.Type.DAS5).to(Das5ResourceProvider.class).asEagerSingleton();

    }

    @Provides
    @Named("Config")
    Config provideConfig(){
        return this.config;
    }

}
