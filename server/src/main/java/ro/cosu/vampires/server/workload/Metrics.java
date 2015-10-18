package ro.cosu.vampires.server.workload;

import autovalue.shaded.com.google.common.common.collect.ImmutableMap;
import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@AutoValue
public abstract class Metrics implements Serializable{

    public abstract ImmutableMap<LocalDateTime, ImmutableMap<String, Double>> timedMetrics();
    public abstract ImmutableMap<String, String> metadata();
    public abstract  String id();


    public static Builder builder() {
        return new AutoValue_Metrics.Builder().id(UUID.randomUUID().toString());
    }

    public static Metrics empty(){

        return new AutoValue_Metrics.Builder()
                .id("empty")
                .timedMetrics(ImmutableMap.<LocalDateTime, ImmutableMap<String, Double>>of())
                .metadata(ImmutableMap.<String, String>of())
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {


        public abstract Builder metadata (ImmutableMap<String, String> metadata);

        public abstract Builder timedMetrics (ImmutableMap<LocalDateTime, ImmutableMap<String, Double>> metrics);

        public abstract Builder id(String id);

        public abstract Metrics build();
    }




}
