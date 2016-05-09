package ro.cosu.vampires.server.workload;


import com.google.auto.value.AutoValue;

import java.time.LocalDateTime;
import java.util.UUID;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class Execution {

    public static Builder builder() {
        return new AutoValue_Execution.Builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .id(UUID.randomUUID().toString())
                .status("created");
    }

    public abstract String id();

    public abstract Configuration configuration();

    public abstract Workload workload();

    public abstract ExecutionMode type();

    public abstract String status();

    public abstract LocalDateTime createdAt();

    public abstract LocalDateTime updatedAt();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder configuration(Configuration configuration);

        public abstract Builder workload(Workload workload);

        public abstract Builder type(ExecutionMode type);

        public abstract Builder id(String id);

        public abstract Builder status(String status);

        public abstract Builder createdAt(LocalDateTime createdAt);

        public abstract Builder updatedAt(LocalDateTime createdAt);


        public abstract Execution build();

    }


}
