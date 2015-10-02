package ro.cosu.vampires.server.resources.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.Resource;


public class SshResource extends AbstractResource{
    static final Logger LOG = LoggerFactory.getLogger(SshResource.class);

    public SshResource() {
        super(Resource.Type.SSH);
    }

    @Override
    public void onStart() {
        LOG.debug("ssh starting");
    }

    @Override
    public void onStop() {
        LOG.debug("ssh stopping");
    }



}
