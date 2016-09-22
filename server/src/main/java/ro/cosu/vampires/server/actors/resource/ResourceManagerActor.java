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

package ro.cosu.vampires.server.actors.resource;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.Optional;
import java.util.UUID;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.QueryResource;
import ro.cosu.vampires.server.actors.messages.resource.BootstrapResource;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceModule;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.values.ClientInfo;

public class ResourceManagerActor extends UntypedActor {
    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());
    private ResourceRegistry resourceRegistry = new ResourceRegistry();
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ResourceManager resourceManager;

    public ResourceManagerActor() {
        Injector injector = Guice.createInjector(new ResourceModule(settings.vampires));
        resourceManager = injector.getInstance(ResourceManager.class);
    }

    public static Props props() {
        return Props.create(ResourceManagerActor.class);
    }


    @Override
    public void onReceive(Object message) throws Exception {
        ActorRef sender = getSender();

        if (message instanceof BootstrapResource) {
            // start a resource
            bootstrapResource((BootstrapResource) message);
        } else if (message instanceof QueryResource) {
            // query a specific resource
            queryResource((QueryResource) message, sender);
        } else if (message instanceof ResourceControl.Shutdown) {
            // shutdown everything
            shutdownResources();
        } else if (message instanceof ResourceInfo) {
            // sent by the resource when the state changes
            registerResource((ResourceInfo) message, sender);
        } else if (message instanceof ClientInfo) {
            // sent when a client registers
            registerClient((ClientInfo) message);
        } else if (message instanceof Terminated) {
            // resource terminates
            terminatedResource(sender);
        } else {
            log.debug("unhandled {}", message);
            unhandled(message);
        }
    }


    private void bootstrapResource(BootstrapResource bootstrap) {
        ResourceProvider resourceProvider = resourceManager.getProvider(bootstrap.type()).orElseThrow(()
                -> new IllegalArgumentException("Error getting " + bootstrap.type()
                + " resource provider.")
        );

        Resource.Parameters parameters = resourceProvider.getParameters(bootstrap.name())
                .withId(UUID.randomUUID().toString())
                .withServerId(bootstrap.serverId());

        Resource resource = resourceProvider.create(parameters).orElseThrow(() ->
                new IllegalArgumentException("Error getting resource with parameters:" + parameters));

        ActorRef resourceActor = getContext().actorOf(ResourceActor.props(resource));
        getContext().watch(resourceActor);
        resourceRegistry.addResourceActor(resourceActor, parameters);
        resourceActor.forward(ResourceControl.Start.create(), getContext());
    }

    private void terminatedResource(ActorRef sender) {
        log.debug("terminated {}", sender);
        getContext().unwatch(sender);
        resourceRegistry.removeResource(sender);
        if (resourceRegistry.getResourceActors().isEmpty()) {
            getContext().stop(getSelf());
            log.debug("shutting down resource manager");
        }
    }

    private void registerClient(ClientInfo clientInfo) {
        getContext().watch(getSender());
        resourceRegistry.registerClient(getSender(), clientInfo);
        resourceRegistry.lookupResourceOfClient(clientInfo.id())
                .ifPresent(resourceActor -> resourceActor.forward(clientInfo, getContext()));
        logCurrentClients();
    }

    private void logCurrentClients() {
        log.info("clients {} / resourceActors {} ", resourceRegistry.getRegisteredClients().size(),
                resourceRegistry.getResourceActors().size());
    }

    private void registerResource(ResourceInfo resourceInfo, ActorRef sender) {
        log.debug("resource register {}", resourceInfo);
        resourceRegistry.registerResource(sender, resourceInfo);
    }

    private void queryResource(QueryResource query, ActorRef sender) {
        Optional<ActorRef> resourceOfClient = resourceRegistry.lookupResourceOfClient(query.resourceId());

        if (resourceOfClient.isPresent()) {
            ActorRef resourceActor = resourceOfClient.get();
            resourceActor.tell(query, sender);
        } else {
            log.warning("Query: {} does not match any existing resource", query);
        }
    }

    private void shutdownResources() {

        resourceRegistry.getResourceActors().forEach(r -> r.forward(ResourceControl.Shutdown.create(), getContext()));
        if (resourceRegistry.getResourceActors().isEmpty()) {
            getContext().stop(getSelf());
        }
    }


}
