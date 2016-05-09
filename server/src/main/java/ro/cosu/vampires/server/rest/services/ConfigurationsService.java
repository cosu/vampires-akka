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

package ro.cosu.vampires.server.rest.services;


import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import akka.actor.ActorSystem;
import ro.cosu.vampires.server.workload.Configuration;

public class ConfigurationsService {

    Map<String, Configuration> configurations = Maps.newConcurrentMap();

    @Inject
    private ActorSystem actorSystem;


    public Collection<Configuration> getConfigurations() {
        return configurations.values();
    }


    public Configuration createConfiguration(Configuration configuration) {
        Configuration created = configuration.create();
        configurations.put(created.id(), created);
        return created;
    }

    public Optional<Configuration> updateConfiguration(Configuration configuration) {

        if (configurations.containsKey(configuration.id())) {
            configuration.touch();
            configurations.put(configuration.id(), configuration);
            return Optional.of(configuration);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Configuration> deleteConfiguration(String id) {
        return Optional.ofNullable(configurations.remove(id));

    }

    public Optional<Configuration> getConfiguration(String id) {
        return Optional.ofNullable(configurations.get(id));
    }

}
