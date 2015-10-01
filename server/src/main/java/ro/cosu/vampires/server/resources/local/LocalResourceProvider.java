package ro.cosu.vampires.server.resources.local;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;

public class LocalResourceProvider implements ResourceProvider {


    @Override
    public Resource create() {
        return new LocalResource();
    }
}
