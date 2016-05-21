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

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.BootstrapResource;
import ro.cosu.vampires.server.actors.messages.CreateResource;
import ro.cosu.vampires.server.actors.messages.QueryExecution;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceModule;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Execution;

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

    private void createResource(CreateResource create) {
        log.debug("create resource {}", create.type());

        final Optional<ResourceProvider> provider = resourceManager.getProvider(create.type());
        if (provider.isPresent()) {
            ActorRef resourceActor = getContext().actorOf(ResourceActor.props(provider.get()));
            getContext().watch(resourceActor);
            resourceRegistry.addResourceActor(resourceActor);
            resourceActor.forward(create, getContext());
        } else {
            log.error("Error getting {} providerType resource provider.", create.type());
        }
    }

    private void bootstrapResource(ResourceProvider resourceProvider, BootstrapResource bootstrap) {
        Resource.Parameters parameters = resourceProvider.getParameters(bootstrap.name())
                .withServerId(bootstrap.serverId());

        CreateResource create = CreateResource
                .create(bootstrap.type(), parameters);
        createResource(create);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        ActorRef sender = getSender();

        if (message instanceof Execution) {
            final Execution execution = (Execution) message;
            startExecution(execution);
        } else if (message instanceof BootstrapResource) {
            final BootstrapResource bootstrap = (BootstrapResource) message;
            bootstrapResource(bootstrap);
        } else if (message instanceof CreateResource) {
            final CreateResource create = (CreateResource) message;
            createResource(create);
        } else if (message instanceof QueryExecution) {
            final QueryExecution query = (QueryExecution) message;
            queryResource(query, sender);
        } else if (message instanceof ResourceControl.Shutdown) {
            shutdownResources();
        } else if (message instanceof ResourceInfo) {
            final ResourceInfo resourceInfo = (ResourceInfo) message;
            registerResource(resourceInfo, sender);
        } else if (message instanceof ClientInfo) {
            final ClientInfo clientInfo = (ClientInfo) message;
            registerClient(clientInfo);
        } else if (message instanceof Terminated) {
            terminatedResource(sender);
        } else {
            log.debug("unhandled {}", message);
            unhandled(message);
        }
    }

    private void startExecution(Execution execution) {
        // if sample then use only 1 instance
        execution
                .configuration().withMode(execution.type())
                .resources()
                .stream()
                .map(resourceDemand -> BootstrapResource.create(
                        resourceDemand.provider(), resourceDemand.type(), execution.id())
                )
                .forEach(bootstrapResource -> getSelf().tell(bootstrapResource, getSender()));
    }

    private void bootstrapResource(BootstrapResource bootstrap) {
        resourceManager.getProvider(bootstrap.type()).ifPresent(rp -> bootstrapResource(rp, bootstrap));
    }

    private void terminatedResource(ActorRef sender) {
        log.debug("terminated {}", sender);
        resourceRegistry.removeResource(sender);
        if (resourceRegistry.getResourceActors().isEmpty()) {
            getContext().stop(getSelf());
            log.debug("shutting down resource manager");
        }
    }

    private void registerClient(ClientInfo clientInfo) {
        getContext().watch(getSender());
        log.debug("watch {}", getSender());
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

    private void queryResource(QueryExecution query, ActorRef sender) {
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
    }


}
