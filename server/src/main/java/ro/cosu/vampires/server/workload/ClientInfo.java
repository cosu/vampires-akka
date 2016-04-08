package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson

public abstract class ClientInfo implements Serializable {

    public static Builder builder() {
        return new AutoValue_ClientInfo.Builder().start(LocalDateTime.now());
    }

    public abstract String id();

    public abstract LocalDateTime start();

    public abstract Map<String, Integer> executors();

    public abstract Metrics metrics();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder start(LocalDateTime start);

        public abstract Builder executors(Map<String, Integer> executors);

        public abstract Builder metrics(Metrics metrics);

        public abstract Builder id(String id);

        public abstract ClientInfo build();


    }
}
