package ro.cosu.vampires.das5;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

public class Das5ResourceProvider extends AbstractResourceProvider{
    @Override
    public Resource create() {
        return new Das5Resource();
    }
}
