package ro.cosu.vampires.server.resources.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ssh.SshResource;


public class LocalResource extends AbstractResource{
    static final Logger LOG = LoggerFactory.getLogger(SshResource.class);


    public LocalResource() {
        super(Resource.Type.LOCAL);
    }

    @Override
    public void onStart() {
        LOG.debug("local starting");
    }

    @Override
    public void onStop() {
        LOG.debug("local stopping");
    }

}
