package ro.cosu.vampires.server.workload;


import com.google.auto.value.AutoValue;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;

import com.typesafe.config.Config;

import java.util.List;
import java.util.stream.Collectors;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class ConfigurationPayload {

    public static ConfigurationPayload fromConfig(Config config) {

        String description = config.hasPath("description") ? config.getString("description") : "";

        List<ResourceDemand> resourceDemandsList = config.getConfigList("start")
                .stream().map(demandConfig -> {
                    String type = demandConfig.getString("type");
                    int count = demandConfig.getInt("count");

                    Resource.ProviderType providerType = Enums.stringConverter
                            (Resource.ProviderType.class).convert(demandConfig.getString("provider").toUpperCase());
                    return ResourceDemand.builder().count(count).type(type)
                            .provider(providerType).build();
                }).collect(Collectors.toList());

        ImmutableList<ResourceDemand> resourceDemands = ImmutableList.copyOf(resourceDemandsList);

        return new AutoValue_ConfigurationPayload(description, resourceDemands);
    }

    public static ConfigurationPayload create(String description, ImmutableList<ResourceDemand> resourceDemands) {
        return new AutoValue_ConfigurationPayload(description, resourceDemands);
    }

    public abstract String description();

    public abstract ImmutableList<ResourceDemand> resources();
}
