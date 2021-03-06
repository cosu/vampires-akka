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

package ro.cosu.vampires.server.actors.workload;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.configuration.CreateConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.DeleteConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.QueryConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.ResponseConfiguration;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.resources.Configuration;
import ro.cosu.vampires.server.values.resources.ProviderDescription;
import ro.cosu.vampires.server.values.resources.ResourceDemand;
import ro.cosu.vampires.server.values.resources.ResourceDescription;

public class ConfigurationsActor extends AbstractActor {

    private final SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private HashBasedTable<User, String, Configuration> table = HashBasedTable.create();

    public static Props props() {
        return Props.create(ConfigurationsActor.class);
    }

    private static double getCost(Configuration configuration, Map<Resource.ProviderType, ProviderDescription> providers) {

        return configuration.resources().stream().map(resource -> Optional
                .ofNullable(providers.get(resource.resourceDescription().provider()))
                .map(provider -> provider.resourceDescriptions().get(resource.resourceDescription().resourceType()))
                .map(ResourceDescription::cost)
                .orElse(0.))
                .collect(Collectors.summingDouble(Double::doubleValue));
    }

    private Optional<ResourceDemand> resolveResourceDemand(ResourceDemand resourceDemand) {
        Map<Resource.ProviderType, ProviderDescription> providers = settings.getProviders();
        ResourceDescription resourceDescription = resourceDemand.resourceDescription();

        if (!providers.containsKey(resourceDescription.provider())) {
            log.error("Unable to resolve provider {}", resourceDescription.provider());
            return Optional.empty();
        }

        ProviderDescription provider = providers.get(resourceDescription.provider());
        if (!provider.resourceDescriptions().containsKey(resourceDescription.resourceType())) {
            log.error("Unable to resolve resource {}", resourceDescription.resourceType());
            return Optional.empty();
        }

        ResourceDescription rd = provider.resourceDescriptions().get(resourceDescription.resourceType());
        return Optional.of(resourceDemand.withResourceDescription(rd));
    }

    private Map<String, Configuration> getUserStore(User user) {
        return table.row(user);
    }

   @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateConfiguration.class, this::handleCreation)
                .match(QueryConfiguration.class, this::handleQuery)
                .match(DeleteConfiguration.class, this::handleDeletion)
                .build();
    }

    private void handleDeletion(DeleteConfiguration deleteConfiguration) {

        Map<String, Configuration> userStore = getUserStore(deleteConfiguration.user());

        List<Configuration> deleted = deleteConfiguration.configurations().stream().filter(userStore::containsKey)
                .map(userStore::remove).collect(Collectors.toList());

        getSender().tell(ResponseConfiguration.create(deleted), getSelf());
    }

    private void handleQuery(QueryConfiguration message) {
        Map<String, Configuration> userStore = getUserStore(message.user());

        List<Configuration> configurations;
        if (message.resources().isEmpty())
            configurations = ImmutableList.copyOf(userStore.values());
        else
            configurations = message.resources().stream().filter(userStore::containsKey)
                    .map(userStore::get).collect(Collectors.toList());

        ResponseConfiguration responseConfiguration = ResponseConfiguration.create(configurations);

        getSender().tell(responseConfiguration, getSelf());
    }

    private void handleCreation(CreateConfiguration message) {

        List<Optional<ResourceDemand>> resolvedDescriptions = message.configuration().resources().stream()
                .map(this::resolveResourceDemand).collect(Collectors.toList());
        boolean allResolved = resolvedDescriptions.stream().allMatch(Optional::isPresent);

        if (allResolved) {
            List<ResourceDemand> resourceDemands = resolvedDescriptions.stream().map(Optional::get).collect(Collectors.toList());
            Configuration configuration = message.configuration().withResources(resourceDemands);
            getUserStore(message.user()).put(configuration.id(), configuration);
            log.debug("Created configuration {}", configuration);
            getSender().tell(ResponseConfiguration.create(Collections.singletonList(configuration)), getSelf());
        } else {
            log.error("Unable to create configuration");
            getSender().tell(ResponseConfiguration.create(Collections.emptyList(), "Unable to resolve resource demands"), getSelf());
        }
    }


}
