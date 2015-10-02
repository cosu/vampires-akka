package ro.cosu.vampires.server.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public abstract class AbstractResource implements Resource {

    static final Logger LOG = LoggerFactory.getLogger(AbstractResource.class);

    private final ResourceDescription description;
    private Resource.Status status = Status.UNKNOWN;

    public AbstractResource(Resource.Type type ) {
        String id = UUID.randomUUID().toString();
        this.description = ResourceDescription.create(id, type);

        LOG.debug("{}", description);
        setStatus(Status.CREATING);
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
    public ResourceDescription getDescription() {
        return description;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        LOG.debug("{} => {}", this, status);
        this.status = status;
    }

    @Override
    public String toString() {
        return "AbstractResource{"
                + "description=" + description+ ", "
                + "status=" + status
                + "}";
    }
}
