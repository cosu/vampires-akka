package ro.cosu.vampires.server.resources;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;

public abstract class AbstractResourceProvider implements ResourceProvider {

    private Config config;

    @Inject
    public void setConfig(@Named("Config") Config config) {
        this.config = config;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    protected Config getConfigForInstance(String instanceName) {
        Config appDefaults = getConfig().getConfig("resources");
        Config providerDefaults = getConfig().getConfig("resources." + getType().toString().toLowerCase());

        return  getSimpleConfigForInstance(instanceName)
                .withFallback(providerDefaults)
                .withFallback(appDefaults);
    }

    protected Config getSimpleConfigForInstance(String instanceName){
        return getConfig().getConfig(getInstanceKey(instanceName));
    }

    protected String getInstanceKey(String instanceName){
        return "resources."+ getType().toString().toLowerCase() + "." + instanceName.toLowerCase();
    }

    @Override
    public Resource.Parameters getParameters(String instanceName) {
        return  getBuilder().fromConfig(getConfigForInstance(instanceName)).build();
    }


}
