package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;

import com.typesafe.config.Config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class Configuration {

    //[
//        {
//        "id": "37ecfc16-f0fc-4c43-a915-bfef7ac6116b",
//        "created_at": "2016-08-05T08:40:51.620Z",
//        "lastupdate_at": "2016-08-05T08:40:51.620Z",
//        "description" : "my optional description",
//        "resources":
//        [
//        {
//        "provider": "ec2",
//        "providerType": "eu-west1.t2.micro",
//        "count": "5"
//        },
//        {
//        "provider": "ec2",
//        "providerType": "eu-west1.t2.nano",
//        "count": "5"
//        }
//        ],
//        "workload": "041363e3-6551-4202-947a-9fa8dab240ec",
//        "cost": 100
//        }
//        ]


    public static Builder builder() {
        return new AutoValue_Configuration.Builder().id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public static Configuration fromConfig(Config config) {

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

        return builder().createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .resources(resourceDemands)
                .description(description)
                .build();
    }

    public Configuration touch() {
        return toBuilder().updatedAt(LocalDateTime.now()).build();
    }

    public abstract String id();

    @Nullable
    public abstract LocalDateTime createdAt();

    @Nullable
    public abstract LocalDateTime updatedAt();

    @Nullable
    public abstract String description();

    public abstract ImmutableList<ResourceDemand> resources();

    public abstract Builder toBuilder();

    public Configuration withResources(ImmutableList<ResourceDemand> resources) {
        return toBuilder().resources(resources).build();
    }

    public Configuration create() {
        return toBuilder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Configuration withMode(ExecutionMode mode) {
        if (mode.equals(ExecutionMode.SAMPLE))
            return toBuilder().resources(
                    ImmutableList.copyOf(
                            resources().stream().map(r -> r.withCount(1)
                            ).collect(Collectors.toList()))).build();
        else return this;
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder id(String id);

        public abstract Builder resources(ImmutableList<ResourceDemand> resources);

        public abstract Builder description(String format);

        public abstract Builder createdAt(LocalDateTime createdAt);

        public abstract Builder updatedAt(LocalDateTime createdAt);

        public abstract Configuration build();
    }

}


