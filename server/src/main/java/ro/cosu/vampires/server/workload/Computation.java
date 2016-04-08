package ro.cosu.vampires.server.workload;


import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.util.UUID;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class Computation implements Serializable {

    public static final String BACKOFF = "BACKOFF";
    public static final String EMPTY = "EMPTY";

    public static Builder builder() {
        return new AutoValue_Computation.Builder().id(UUID.randomUUID().toString());
    }

    public static Computation backoff(int backoffInterval) {
        return new AutoValue_Computation.Builder()
                .id(BACKOFF)
                .command("sleep " + backoffInterval)
                .build();
    }

    public static Computation withCommand(String command) {
        return builder().command(command).build();
    }

    public static Computation empty() {
        return new AutoValue_Computation.Builder()
                .id(EMPTY)
                .command("true")
                .build();
    }

    public abstract String id();

    public abstract String command();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder command(String id);

        public abstract Computation build();
    }

}
