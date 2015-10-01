package ro.cosu.vampires.server.resources.ssh;

import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.Resource;


public class SshResource extends AbstractResource{

    public SshResource() {
        super(Resource.Type.SSH);
    }

    @Override
    public void start() {
        super.start();
        System.out.println("ssh start");
    }

    @Override
    public void stop() {
        super.stop();
        System.out.println("ssh stop");

    }



}
