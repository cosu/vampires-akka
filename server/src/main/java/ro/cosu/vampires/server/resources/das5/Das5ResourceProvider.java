package ro.cosu.vampires.server.resources.das5;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

public class Das5ResourceProvider extends AbstractResourceProvider{
    @Override
    public Resource create() {
        String command = getConfig().getString("command");
        String user = getConfig().getString("user");
        String privateKey = getConfig().getString("privateKey");
        String address  = getConfig().getString("address");
        int port = getConfig().hasPath("port") ? getConfig().getInt("port") : 22;

        return new Das5Resource(user, privateKey, address, command, port);
    }
}
