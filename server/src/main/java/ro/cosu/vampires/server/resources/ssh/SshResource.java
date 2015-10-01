package ro.cosu.vampires.server.resources.ssh;

import ro.cosu.vampires.server.resources.IResource;

public class SshResource implements IResource {

    @Override
    public void start() {
        System.out.println("ssh start");
    }

    @Override
    public void stop() {
        System.out.println("ssh stop");

    }
}
