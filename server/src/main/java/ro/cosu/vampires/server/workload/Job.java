package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;
import ro.cosu.vampires.server.util.gson.AutoGson;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@AutoValue
@AutoGson
public abstract class Job implements Serializable {


    public abstract String id();

    public abstract String from();

    public abstract LocalDateTime created();

    public abstract JobStatus status();

    public abstract Computation computation();

    public abstract Result result();

    public abstract Metrics hostMetrics();



    public abstract Builder toBuilder();

    public Job withCommand(String command) {
        return toBuilder().computation(Computation.withCommand(command)).build();
    }

    public Job from(String from) {
        return toBuilder()
                .from(from)
                .build();
    }


    public Job withHostMetrics(Metrics metrics) {
        return toBuilder().hostMetrics(metrics)
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
                .hostMetrics(Metrics.empty())
                .result(Result.empty()).build();
    }
    public static Job backoff( int backoffInterval) {
        return builder().computation(Computation.backoff(backoffInterval))
                .hostMetrics(Metrics.empty())
                .result(Result.empty()).build();
    }

    public static Builder builder() {
        return new AutoValue_Job.Builder()
                .created(LocalDateTime.now())
                .id(UUID.randomUUID().toString())
                .from("")
                .status(JobStatus.NEW);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder computation(Computation computation);

        public abstract Builder result(Result result);

        public abstract Builder hostMetrics(Metrics metrics);

        public abstract Builder created(LocalDateTime created);

        public abstract Builder from(String from);

        public abstract Builder status(JobStatus jobstatus);

        public abstract Builder id(String id);

        public abstract Job build();
    }
}
