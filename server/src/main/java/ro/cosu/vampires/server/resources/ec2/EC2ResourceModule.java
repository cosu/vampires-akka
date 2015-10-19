package ro.cosu.vampires.server.resources.ec2;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;

public class EC2ResourceModule extends AbstractModule{
    @Override
    protected void configure() {
        MapBinder<Resource.Type, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);
        mapbinder.addBinding(Resource.Type.EC2).to(EC2ResourceProvider.class).in(Scopes.SINGLETON);
    }

}
