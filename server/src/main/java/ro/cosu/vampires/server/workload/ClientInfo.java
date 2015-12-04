package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.util.Map;

@AutoValue
public abstract class ClientInfo  implements Serializable {

    public abstract Map<String, Integer> executors();
    public abstract Metrics metrics();

    public static Builder builder() {
        return new AutoValue_ClientInfo.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder executors (Map<String, Integer> executors);

        public abstract Builder metrics(Metrics metrics);

        public abstract ClientInfo build();


    }
}
