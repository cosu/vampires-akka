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
import com.google.common.collect.Maps;
import com.google.inject.TypeLiteral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.User;


public class ConfigurationsService implements Service<Configuration, ConfigurationPayload> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsService.class);

    private Map<String, Configuration> configurations = Collections.synchronizedSortedMap(Maps.newTreeMap());

    public static TypeLiteral<Service<Configuration, ConfigurationPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Configuration, ConfigurationPayload>>() {
        };
    }

    @Override
    public Collection<Configuration> list(User user) {
        return configurations.values();
    }

    @Override
    public Configuration create(ConfigurationPayload payload, User user) {
        Configuration created = Configuration.fromPayload(payload);
        configurations.put(created.id(), created);
        LOG.debug("Created  {} : {}", created.id(), created);
        return created;
    }

    @Override
    public Optional<Configuration> delete(String id, User user) {
        return Optional.ofNullable(configurations.remove(id));
    }

    @Override
    public Optional<Configuration> update(ConfigurationPayload updated, User user) {
        Preconditions.checkNotNull(updated, "empty payload");
        Preconditions.checkNotNull(updated.id(), "id must not be empty");

        if (configurations.containsKey(updated.id())) {
            Configuration configuration = configurations.get(updated.id());

            configurations.put(updated.id(), configuration);
            return Optional.of(configuration);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Configuration> get(String id, User user) {
        return Optional.ofNullable(configurations.get(id));
    }
}
