package ro.cosu.vampires.server.workload;


import com.google.auto.value.AutoValue;
import ro.cosu.vampires.server.util.gson.AutoGson;

import java.io.Serializable;
import java.util.UUID;

@AutoValue
@AutoGson
public abstract class Computation implements Serializable {

    public abstract  String id();
    public abstract  String command();

    public static Builder builder() {
        return new AutoValue_Computation.Builder().id(UUID.randomUUID().toString());
    }

    public static Computation backoff() {
        return new AutoValue_Computation.Builder()
                .id("BACKOFF")
                .command("sleep 5")
                .build();
    }

    public static Computation empty() {
        return new AutoValue_Computation.Builder()
                .id("EMPTY")
                .command("true")
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);
        public abstract Builder command(String id);

        public abstract Computation build();
    }

}
