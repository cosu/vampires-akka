package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.UUID;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson

public abstract class Metrics implements Serializable {

    public static Builder builder() {
        return new AutoValue_Metrics.Builder().id(UUID.randomUUID().toString());
    }

    public static Metrics empty() {

        return new AutoValue_Metrics.Builder()
                .id("empty")
                .metrics(ImmutableList.of())
                .metadata(ImmutableMap.of())
                .build();
    }

    public abstract ImmutableList<Metric> metrics();

    public abstract ImmutableMap<String, String> metadata();

    public abstract String id();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {


        public abstract Builder metadata(ImmutableMap<String, String> metadata);

        public abstract Builder metrics(ImmutableList<Metric> metrics);

        public abstract Builder id(String id);

        public abstract Metrics build();
    }


}
