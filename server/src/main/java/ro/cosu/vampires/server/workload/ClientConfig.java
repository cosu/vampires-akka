package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;

import java.io.Serializable;

@AutoValue
public abstract  class ClientConfig implements Serializable {

    public abstract String executor();

    public abstract int cpuSetSize();

    public abstract int numberOfExecutors();


    public static Builder withDefaults() {return builder().cpuSetSize(1).executor("FORK");}
    public static Builder builder() {
        return new AutoValue_ClientConfig.Builder();
    }


    public static ClientConfig empty() {
        return builder()
                .cpuSetSize(1)
                .executor("EMPTY")
                .numberOfExecutors(0)
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder executor(String executor);

        public abstract Builder cpuSetSize(int cpuSetSize);

        public abstract Builder numberOfExecutors(int cpuSetSize);

        public abstract ClientConfig build();
    }
}
