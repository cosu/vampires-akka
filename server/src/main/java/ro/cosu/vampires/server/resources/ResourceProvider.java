package ro.cosu.vampires.server.resources;

import com.typesafe.config.Config;

import java.util.Optional;

public interface ResourceProvider {
    Optional<Resource> create(Resource.Parameters parameters);

    Resource.Type getType();

    Config getConfig();

    Resource.Parameters getParameters(String instanceName);

    Resource.Parameters.Builder getBuilder();

}
