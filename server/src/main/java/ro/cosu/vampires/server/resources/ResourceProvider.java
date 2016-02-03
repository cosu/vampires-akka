package ro.cosu.vampires.server.resources;

import com.typesafe.config.Config;

import java.util.Optional;

public interface ResourceProvider {
    Optional<Resource> create(Resource.Parameters parameters);

    Resource.Type getType();

    Config getConfig();

    default Config getConfigForInstance(String instanceName) {
        Config appDefaults = getConfig().getConfig("resources");
        Config providerDefaults = getConfig().getConfig("resources." + getType().toString().toLowerCase());

        return getConfig().getConfig("resources."+ getType().toString().toLowerCase() + "." + instanceName.toLowerCase())
                .withFallback(providerDefaults)
                .withFallback(appDefaults);
    }

    default Resource.Parameters getParameters(String instanceName) {
        return  getBuilder().fromConfig(getConfigForInstance(instanceName)).build();
    }

    Resource.Parameters.Builder getBuilder();
}
