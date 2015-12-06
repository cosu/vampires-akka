package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@AutoValue
public abstract class ClientInfo  implements Serializable {

    public abstract  String id();

    public abstract LocalDateTime start();

    public abstract Map<String, Integer> executors();

    public abstract Metrics metrics();

    public static Builder builder() {
        return new AutoValue_ClientInfo.Builder().start(LocalDateTime.now());
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder start(LocalDateTime start);

        public abstract Builder executors (Map<String, Integer> executors);

        public abstract Builder metrics(Metrics metrics);

        public abstract Builder id(String id);

        public abstract ClientInfo build();


    }
}
