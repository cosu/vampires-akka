package ro.cosu.vampires.server.resources.ssh;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;

public class SshResourceProvider implements ResourceProvider{
    @Override
    public Resource create() {
        return new SshResource();
    }
}
