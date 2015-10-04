package ro.cosu.vampires.server.resources.ssh;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

public class SshResourceProvider  extends AbstractResourceProvider {
    @Override
    public Resource create() {
        String command = getConfig().getString("command");
        String user = getConfig().getString("user");
        String privateKey = getConfig().getString("privateKey");
        String address  = getConfig().getString("address");
        return new SshResource(user, privateKey, address, command);
    }
}
