package ro.cosu.vampires.server.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AbstractResource implements Resource {

    static final Logger LOG = LoggerFactory.getLogger(AbstractResource.class);

    private final ResourceDescription description;
    private Resource.Status status = Status.CREATING;

    public AbstractResource(Resource.Type type ) {
        String id = UUID.randomUUID().toString();
        this.description = ResourceDescription.create(id, type);
        LOG.debug("{}", description);
    }


    public void start() {
        this.status = Status.STARTING;
        this.onStart();
        // TODO this should be a future
    }

    public void stop() {
        this.onStop();
        this.status = Status.STOPPED;
        // TODO this should be a future
    }

    @Override
    public void onStart() {
        // nothing
    }

    @Override
    public void onStop() {
        // nothing
    }

    @Override
    public ResourceDescription getDescription() {
        return description;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
