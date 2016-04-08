package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.time.LocalDateTime;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class Metric implements Serializable {

    public static Builder builder() {
        return new AutoValue_Metric.Builder();
    }

    public static Metric empty() {

        return new AutoValue_Metric.Builder()
                .values(ImmutableMap.of())
                .time(LocalDateTime.parse("2000-01-01T00:00:00"))
                .build();
    }

    public abstract ImmutableMap<String, Double> values();

    public abstract LocalDateTime time();

    @AutoValue.Builder
    public abstract static class Builder {


        public abstract Builder time(LocalDateTime time);

        public abstract Builder values(ImmutableMap<String, Double> values);

        public abstract Metric build();
    }


}
