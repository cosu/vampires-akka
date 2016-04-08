package ro.cosu.vampires.server.resources;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public abstract class AbstractResource implements Resource {

    private final Parameters parameters;
    private final ResourceDescription description;
    private Resource.Status status;
    public AbstractResource(Resource.Parameters parameters) {
        this.parameters = parameters;
        setStatus(Status.SLEEPING);
        this.description = ResourceDescription.create(generateId(), parameters.type());
        getLogger().debug("resource parameters {}", parameters);
        getLogger().debug("creating resource with description {}", description);
    }

    protected abstract Logger getLogger();

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    public CompletableFuture<Resource> start() {
        return CompletableFuture.supplyAsync(this::startCall)
                .exceptionally(this::fail);
    }

    public CompletableFuture<Resource> stop() {
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
        getLogger().debug("failed resource", ex);
        try {
            this.onFail();
        } catch (Exception e) {
            throw new CompletionException(e);
        } finally {
            setStatus(Status.FAILED);
        }
        return this;
    }

    public Resource stopCall() {
        if (status.equals(Status.STOPPED) || status.equals(Status.STOPPING))
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
    public void connected() {
        setStatus(Status.CONNECTED);
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
        getLogger().debug("{} => {}", this, status);
        //TODO check for illegal state transition
        this.status = status;
    }

    @Override
    public String toString() {
        return getLogger().getName() + "{"
                + "description=" + description + ", "
                + "status=" + status
                + "}";
    }

    @VisibleForTesting
    public Parameters getParameters() {
        return parameters;
    }
}
