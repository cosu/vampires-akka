package ro.cosu.vampires.server.resources.local;

import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.Resource;


public class LocalResource extends AbstractResource{

    public LocalResource() {
        super(Resource.Type.LOCAL);
    }

    @Override
    public void onStart() {
        System.out.println("local starting");
    }

    @Override
    public void onStop() {
        System.out.println("local stopping");
    }

}
