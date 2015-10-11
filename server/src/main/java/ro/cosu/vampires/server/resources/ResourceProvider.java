package ro.cosu.vampires.server.resources;

import com.typesafe.config.Config;

public interface ResourceProvider {
    Resource create( Resource.Parameters parameters);

    Resource.Type getType();

    Config getConfig();

    default Config getConfigForInstance(String instanceName) {
        Config providerDefaults = getConfig().getConfig("vampires.resources." + getType().toString().toLowerCase() +
                ".instances");
        Config instanceConfig = getConfig().getConfig("vampires.resources.ssh.instances." + instanceName.toLowerCase()).withFallback(providerDefaults);

        return instanceConfig.withFallback(providerDefaults);
    }


    default Resource create(String instanceName) {

        Resource.Parameters parameters = getBuilder().fromConfig(getConfigForInstance(instanceName)).build();

        return  create(parameters );
    }

    Resource.Parameters.Builder getBuilder();


}
