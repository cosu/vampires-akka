package ro.cosu.vampires.server.resources.local;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

public class LocalResourceProvider extends AbstractResourceProvider{



    @Override
    public Resource create() {
        return new LocalResource(getConfig().getString("command"));
    }
}
