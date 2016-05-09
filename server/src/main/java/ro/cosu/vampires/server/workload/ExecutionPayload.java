package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class ExecutionPayload {

    public static Builder builder() {
        return new AutoValue_ExecutionPayload.Builder();
    }

    public abstract String configuration();

    public abstract String workload();

    public abstract String type();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder configuration(String configuration);

        public abstract Builder workload(String workload);

        public abstract Builder type(String type);

        public abstract ExecutionPayload build();

    }

}
