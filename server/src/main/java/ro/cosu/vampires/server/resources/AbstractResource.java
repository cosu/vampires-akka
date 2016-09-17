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

import com.google.common.collect.Sets;

import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public abstract class AbstractResource implements Resource {

    private final Parameters parameters;
    private Resource.Status status = Status.UNKNOWN;

    public AbstractResource(Resource.Parameters parameters) {
        this.parameters = parameters;
        setStatus(Status.SLEEPING);
    }

    protected abstract Logger getLogger();

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
        Set<Status> invalidStatus = Sets.immutableEnumSet(Status.STOPPED,
                Status.FAILED, Status.STOPPING, Status.UNKNOWN);
        if (invalidStatus.contains(status))
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
    public Parameters parameters() {
        return parameters;
    }


    @Override
    public Status status() {
        return status;
    }

    public void setStatus(Status status) {
        getLogger().debug("{} => {}", toString(), status);
        //TODO check for illegal state transition
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s = %s", parameters.providerType(), parameters.instanceType(), parameters.id(), status);
    }

}
