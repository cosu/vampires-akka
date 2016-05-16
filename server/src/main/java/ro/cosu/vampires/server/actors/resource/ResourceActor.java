/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.actors.resource;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActorWithStash;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import ro.cosu.vampires.server.actors.messages.CreateResource;
import ro.cosu.vampires.server.actors.messages.QueryResource;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.workload.ClientInfo;

public class ResourceActor extends UntypedActorWithStash {
    private final ResourceProvider resourceProvider;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private Resource resource;
    private Procedure<Object> active = message -> {
        ActorRef sender = getSender();

        if (message instanceof QueryResource) {
            sendResourceInfo(sender);
        } else if (message instanceof ClientInfo) {
            connectClient((ClientInfo) message);
        } else if (message instanceof ResourceControl.Shutdown) {
            log.debug("shutdown " + message);
            Resource stoppedResource = resource.stop().get();
            sender.tell(stoppedResource.info(), getSelf());
        } else {
            log.error("unhandled {}", message);
            unhandled(message);
        }
    };

    ResourceActor(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    public static Props props(ResourceProvider resourceProvider) {
        return Props.create(ResourceActor.class, resourceProvider);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        ActorRef sender = getSender();
        if (message instanceof CreateResource) {
            CreateResource create = (CreateResource) message;
            createResource(create, sender);
        } else if (message instanceof QueryResource) {
            sendResourceInfo(sender);
        } else if (message instanceof ResourceInfo) {
            ResourceInfo resourceInfo = (ResourceInfo) message;
            if (Resource.Status.RUNNING.equals(resourceInfo.status())) {
                activate();
            } else {
                log.error("got resource info {}. deactivating actor", resourceInfo);
                fail();
            }
        } else {
            log.debug("stash {}", message);
            stash();
        }
    }

    private void createResource(CreateResource create, ActorRef sender) {
        if (resource != null && !resource.status().equals(Resource.Status.SLEEPING)) {
            log.warning("Attempting to start an already started resource. doing nothing");
            return;
        }

        Optional<Resource> resourceOptional = resourceProvider.create(create.parameters());

        if (resourceOptional.isPresent()) {
            this.resource = resourceOptional.get();
            // do it async because activate needs a context
            // which is not available after the future completes
            this.resource.start().thenAccept(started -> {
                sendResourceInfo(sender);
                sendResourceInfo(getSelf());
            });
        } else {
            getSelf().tell(Resource.Status.FAILED, sender);
        }
    }

    private Void fail() {
        log.error("actor failed to interact with resource ");
        sendInfoToParent();
        getContext().stop(getSelf());
        return null;
    }

    private void sendInfoToParent() {
        sendResourceInfo(getContext().parent());
    }

    private void activate() {
        sendInfoToParent();
        unstashAll();
        getContext().become(active);
    }

    @Override
    public void postStop() {
        if (resource != null)
            try {
                resource.stop().get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e, "failed to stop resource");
            }
    }

    private void connectClient(ClientInfo clientInfo) {
        if (clientInfo.id().equals(resource.description().id())) {
            resource.connected();
            log.info("Connected: {}", resource.info());
        } else {
            log.error("client info and resource info don't match {}, {}", clientInfo, resource.info());
        }
    }

    private void sendResourceInfo(ActorRef toActor) {
        ResourceInfo info = Optional.ofNullable(this.resource)
                .map(Resource::info)
                .orElse(ResourceInfo.failed(resourceProvider.getProviderType()));
        log.info("to actor {}", toActor);
        toActor.tell(info, getSelf());
    }

}