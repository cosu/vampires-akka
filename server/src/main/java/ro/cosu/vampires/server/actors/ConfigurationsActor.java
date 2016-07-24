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

package ro.cosu.vampires.server.actors;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.messages.configuration.CreateConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.DeleteConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.QueryConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.ResponseConfiguration;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.resources.Configuration;
import ro.cosu.vampires.server.values.resources.ConfigurationPayload;
import ro.cosu.vampires.server.values.resources.ResourceDescription;

public class ConfigurationsActor extends UntypedActor {

    private final SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private HashBasedTable<User, String, Configuration> table = HashBasedTable.create();

    public static Props props() {
        return Props.create(ConfigurationsActor.class);
    }

    private Map<String, Configuration> getUserStore(User user) {
        return table.row(user);
    }

    @Override
    public void preStart() {
        User user = settings.getDefaultUser();
        if (settings.vampires.hasPath("configurations")) {
            settings.vampires.getConfigList("configurations").stream()
                    .map(ConfigurationPayload::fromConfig)
                    .map(Configuration::fromPayload)
                    .forEach(c -> getUserStore(user).put(c.id(), c));
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof CreateConfiguration) {
            CreateConfiguration createConfiguration = (CreateConfiguration) message;
            // this is a convoluted way to update the cost
            double sum = createConfiguration.configuration().resources().stream()
                    .mapToDouble(resourceDemand -> settings.getProviders().stream().filter(p -> p.provider()
                            .equals(resourceDemand.resourceDescription().provider()))
                            .findFirst()
                            .map(providerDescription -> providerDescription.resources().stream()
                                    .filter(
                                            resourceDescription -> resourceDescription.type().equals(resourceDemand.resourceDescription().type())
                                    )
                                    .mapToDouble(ResourceDescription::cost).findFirst().orElse(0.)
                            ).orElse(0.)).sum();

            Configuration configuration = createConfiguration.configuration().withCost(sum);
            getUserStore(createConfiguration.user()).put(configuration.id(), configuration);
            getSender().tell(configuration, getSelf());
        } else if (message instanceof QueryConfiguration) {

            QueryConfiguration queryConfiguration = (QueryConfiguration) message;
            Map<String, Configuration> userStore = getUserStore(queryConfiguration.user());

            List<Configuration> configurations;
            if (queryConfiguration.resources().isEmpty())
                configurations = ImmutableList.copyOf(userStore.values());
            else
                configurations = queryConfiguration.resources().stream().filter(userStore::containsKey)
                        .map(userStore::get).collect(Collectors.toList());

            ResponseConfiguration responseConfiguration = ResponseConfiguration.create(configurations);

            getSender().tell(responseConfiguration, getSelf());
        } else if (message instanceof DeleteConfiguration) {
            DeleteConfiguration deleteConfiguration = (DeleteConfiguration) message;

            Map<String, Configuration> userStore = getUserStore(deleteConfiguration.user());

            List<Configuration> deleted = deleteConfiguration.configurations().stream().filter(userStore::containsKey)
                    .map(userStore::remove).collect(Collectors.toList());

            getSender().tell(ResponseConfiguration.create(deleted), getSelf());

        } else {
            log.error("unhandled {}", message);
            unhandled(message);
        }
    }

}
