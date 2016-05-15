package ro.cosu.vampires.server.rest.services;

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


public class ConfigurationsService implements Service<Configuration, ConfigurationPayload> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsService.class);

    private Map<String, Configuration> configurations = Collections.synchronizedSortedMap(Maps.newTreeMap());

    public static TypeLiteral<Service<Configuration, ConfigurationPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Configuration, ConfigurationPayload>>() {
        };
    }

    @Override
    public Collection<Configuration> list() {
        return configurations.values();
    }

    @Override
    public Configuration create(ConfigurationPayload payload) {
        Configuration created = Configuration.fromPayload(payload);
        configurations.put(created.id(), created);
        LOG.debug("Created  {} : {}", created.id(), created);
        return created;
    }

    @Override
    public Optional<Configuration> delete(String id) {
        return Optional.ofNullable(configurations.remove(id));
    }

    @Override
    public Optional<Configuration> update(Configuration updated) {
        if (configurations.containsKey(updated.id())) {
            Configuration configuration = updated.touch();
            configurations.put(updated.id(), configuration);
            return Optional.of(configuration);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Configuration> get(String id) {
        return Optional.ofNullable(configurations.get(id));
    }
}
