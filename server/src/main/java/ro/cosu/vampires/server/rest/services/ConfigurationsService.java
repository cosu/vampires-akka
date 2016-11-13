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

package ro.cosu.vampires.server.rest.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

import java.util.List;
import java.util.Optional;

import akka.actor.ActorRef;
import ro.cosu.vampires.server.actors.messages.configuration.CreateConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.DeleteConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.QueryConfiguration;
import ro.cosu.vampires.server.actors.messages.configuration.ResponseConfiguration;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.resources.Configuration;
import ro.cosu.vampires.server.values.resources.ConfigurationPayload;


public class ConfigurationsService implements Service<Configuration, ConfigurationPayload> {

    @Inject
    private ActorRef actorRef;

    public static TypeLiteral<Service<Configuration, ConfigurationPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Configuration, ConfigurationPayload>>() {
        };
    }

    @Override
    public List<Configuration> list(User user) {
        QueryConfiguration queryConfiguration = QueryConfiguration.all(user);
        return getConfigurations(queryConfiguration);
    }

    private List<Configuration> getConfigurations(QueryConfiguration queryConfiguration) {
        Optional<ResponseConfiguration> ask = ActorUtil.ask(queryConfiguration, actorRef);
        ResponseConfiguration responseConfiguration = ask.orElseThrow(() -> new RuntimeException("failed to get"));
        return responseConfiguration.configurations();
    }

    @Override
    public Configuration create(ConfigurationPayload payload, User user) {
        Configuration created = Configuration.fromPayload(payload);

        Optional<ResponseConfiguration> ask = ActorUtil.ask(CreateConfiguration.create(created, user), actorRef);

        ResponseConfiguration responseConfiguration = ask.orElseThrow(() -> new RuntimeException("failed to create"));

        if (responseConfiguration.configurations().isEmpty()) {
            throw new RuntimeException(responseConfiguration.message());
        }

        return responseConfiguration.configurations().get(0);
    }

    @Override
    public Optional<Configuration> delete(String id, User user) {
        DeleteConfiguration deleteConfiguration = DeleteConfiguration.create(Lists.newArrayList(id), user);
        Optional<ResponseConfiguration> ask = ActorUtil.ask(deleteConfiguration, actorRef);
        ResponseConfiguration responseConfiguration = ask.orElseThrow(() -> new RuntimeException("failed to delete"));
        List<Configuration> configurations = responseConfiguration.configurations();
        if (configurations.isEmpty()) {
            return Optional.empty();
        } else
            return Optional.of(configurations.get(0));
    }

    @Override
    public Optional<Configuration> update(ConfigurationPayload payload, User user) {
        Preconditions.checkNotNull(payload, "empty payload");
        Preconditions.checkNotNull(payload.id(), "id must not be empty");

        Optional<Configuration> currentOptional = get(payload.id(), user);

        if (currentOptional.isPresent()) {
            Configuration configuration = currentOptional.get();
            configuration = configuration.updateFromPayload(payload);
            CreateConfiguration createConfiguration = CreateConfiguration.create(configuration, user);
            return ActorUtil.ask(createConfiguration, actorRef);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Configuration> get(String id, User user) {
        QueryConfiguration queryConfiguration = QueryConfiguration.create(id, user);
        List<Configuration> configurations = getConfigurations(queryConfiguration);
        if (configurations.isEmpty()) {
            return Optional.empty();
        } else
            return Optional.of(configurations.get(0));
    }
}
