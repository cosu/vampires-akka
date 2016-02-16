package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;
import ro.cosu.vampires.server.util.gson.AutoGson;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;


@AutoValue
@AutoGson
public abstract class Trace implements Serializable{

    public abstract LocalDateTime start();

    public abstract LocalDateTime stop();

    public abstract Metrics executorMetrics();

    public abstract Set<Integer> cpuSet();

    public abstract int totalCpuCount();

    public abstract String executor();

    public static Trace empty() {
        return builder().cpuSet(Sets.newHashSet())
                .start(LocalDateTime.parse("2000-01-01T00:00:00"))
                .stop(LocalDateTime.parse("2000-01-01T00:00:00"))
                .totalCpuCount(0)
                .executorMetrics(Metrics.empty())
                .executor("unknown")
                .build();
    }

    public static Builder withNoMetrics() {
        return builder()
                .executorMetrics(Metrics.empty());
    }

    public static Builder builder() {
        return new AutoValue_Trace.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder executor(String executor);

        public abstract Builder executorMetrics(Metrics metrics);

        public abstract Builder cpuSet(Set<Integer> cpuSet);

        public abstract Builder start(LocalDateTime start);

        public abstract Builder stop(LocalDateTime stop);

        public abstract Builder totalCpuCount(int totalCpuCount);

        public abstract Trace build();
    }


}
