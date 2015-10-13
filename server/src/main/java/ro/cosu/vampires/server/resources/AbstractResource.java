package ro.cosu.vampires.server.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public abstract class AbstractResource implements Resource {

    static final Logger LOG = LoggerFactory.getLogger(AbstractResource.class);

    private final ResourceDescription description;
    private Resource.Status status = Status.UNKNOWN;

    public AbstractResource(Resource.Parameters parameters) {

        this.description = ResourceDescription.create(generateId(), parameters.type());

        LOG.debug("{}", description);
        setStatus(Status.CREATING);


    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    public CompletableFuture<Resource> start(){

        return CompletableFuture.supplyAsync(this::startCall)
                .exceptionally(this::fail);
    }

    public CompletableFuture<Resource> stop(){
        return CompletableFuture.supplyAsync(this::stopCall)
                .exceptionally(this::fail);

    }

    public Resource startCall() {
        setStatus(Status.STARTING);
        try {
            this.onStart();
        } catch (Exception e) {
            throw new CompletionException(e);
        }
        setStatus(Status.RUNNING);
        return this;
    }

    public Resource fail(Throwable ex) {
        LOG.debug("failed resource", ex);
        try {
            this.onFail();
        } catch (Exception e) {
            throw new CompletionException(e);
        }
        finally {
            setStatus(Status.FAILED);
        }
        return this;
    }

    public Resource stopCall() {
        if (status.equals(Status.STOPPED) )
            return this;
        setStatus(Status.STOPPING);
        try {
            this.onStop();
        } catch (Exception e) {
            throw new CompletionException(e);
        }
        setStatus(Status.STOPPED);
        return this;
    }



    @Override
    public ResourceDescription description() {
        return description;
    }



    @Override
    public Status status() {
        return status;
    }

    public void setStatus(Status status) {
        LOG.debug("{} => {}", this, status);
        //TODO check for illegal state transition
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
