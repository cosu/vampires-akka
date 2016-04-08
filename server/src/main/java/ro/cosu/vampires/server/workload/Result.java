package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class Result implements Serializable {

    public static Builder builder() {
        return new AutoValue_Result.Builder();
    }

    public static Result empty() {
        return new AutoValue_Result.Builder()
                .output(new LinkedList<>())
                .exitCode(-1)
                .trace(Trace.empty())
                .duration(0)
                .build();
    }

    public abstract List<String> output();

    public abstract int exitCode();

    public abstract long duration();

    public abstract Trace trace();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder output(List<String> output);

        public abstract Builder exitCode(int exitCode);

        public abstract Builder duration(long duration);

        public abstract Builder trace(Trace trace);

        public abstract Result build();


    }
}
