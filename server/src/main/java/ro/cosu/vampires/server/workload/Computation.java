package ro.cosu.vampires.server.workload;


import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.util.UUID;

@AutoValue
public abstract class Computation implements Serializable {

    public abstract  String id();
    public abstract  String command();

    public static Builder builder() {
        return new AutoValue_Computation.Builder().id(UUID.randomUUID().toString());
    }


    public static Computation empty() {
        return new AutoValue_Computation.Builder()
                .id("EMPTY")
                .command("EMPTY")
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);
        public abstract Builder command(String id);

        public abstract Computation build();
    }

}
