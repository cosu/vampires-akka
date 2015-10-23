package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@AutoValue
public abstract class Job implements Serializable {


    public abstract Computation computation();
    public abstract LocalDateTime created ();

    public abstract Result result();

    public abstract JobStatus status();

    public abstract Metrics metrics();
    public abstract String id();

    public abstract Builder toBuilder();

    public Job withMetrics(Metrics metrics) {
        return toBuilder().metrics(metrics)
                .status(JobStatus.COMPLETE)
                .build();
    }

    public Job withResult(Result result) {
        return toBuilder().result(result)
                .status(JobStatus.EXECUTED)
                .build();
    }

    public Job withComputation(Computation computation) {
        return toBuilder().computation(computation).build();
    }


    public static Job empty() {
        return builder().computation(Computation.empty())
                .metrics(Metrics.empty())
                .status(JobStatus.NEW)
                .result(Result.empty()).build();
    }

    public static Builder builder() {
        return new AutoValue_Job.Builder().created(LocalDateTime.now()).status(JobStatus.NEW).id(UUID.randomUUID()
                .toString());
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder computation(Computation computation);
        public abstract Builder result (Result result);
        public abstract Builder metrics (Metrics metrics);
        public abstract Builder created (LocalDateTime created);

        public abstract Builder status(JobStatus jobstatus);
        public abstract Builder id(String id);

        public abstract Job build();
    }
}
