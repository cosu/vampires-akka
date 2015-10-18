package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@AutoValue
public abstract class Workload implements Serializable {
    public abstract Computation computation();
    public abstract LocalDateTime created ();


    public abstract Result result();

    public abstract Metrics metrics();
    public abstract String id();

    public abstract Builder toBuilder();

    public Workload withMetrics(Metrics metrics) {
        return toBuilder().metrics(metrics).build();
    }

    public Workload withResult(Result result) {
        return toBuilder().result(result).build();
    }

    public Workload withComputation(Computation computation) {
        return toBuilder().computation(computation).build();
    }


    public static Workload empty() {
        return builder().computation(Computation.empty()).metrics(Metrics.empty()).result(Result.empty()).build();

//                .build();
    }

    public static Builder builder() {
        return new AutoValue_Workload.Builder().created(LocalDateTime.now()).id(UUID.randomUUID().toString());
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder computation(Computation computation);
        public abstract Builder result (Result result);
        public abstract Builder metrics (Metrics metrics);
        public abstract Builder created (LocalDateTime created);
        public abstract Builder id(String id);

        public abstract Workload build();
    }
}
