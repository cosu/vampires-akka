/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.resources;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public abstract class AbstractResource implements Resource {

    private final Parameters parameters;
    private final ResourceId description;
    private Resource.Status status;
    public AbstractResource(Resource.Parameters parameters) {
        this.parameters = parameters;
        setStatus(Status.SLEEPING);
        this.description = ResourceId.create(generateId(), parameters.providerType());
        getLogger().debug("resource parameters {}", parameters);
        getLogger().debug("creating resource with properties {}", description);
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
    public ResourceId description() {
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
                + "properties=" + description + ", "
                + "info=" + status
                + "}";
    }

    @VisibleForTesting
    public Parameters getParameters() {
        return parameters;
    }
}
