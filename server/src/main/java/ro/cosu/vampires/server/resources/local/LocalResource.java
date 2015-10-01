package ro.cosu.vampires.server.resources.local;

import ro.cosu.vampires.server.resources.IResource;

public class LocalResource implements IResource {


    @Override
    public void start() {
        System.out.println("local starting");
    }

    @Override
    public void stop() {
        System.out.println("local stopping");
    }
}
