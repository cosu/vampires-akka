package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;


@AutoValue
public abstract class ExecInfo implements Serializable{

    public abstract LocalDateTime start();

    public abstract LocalDateTime stop();

    public abstract Metrics metrics();

    public abstract Set<Integer> cpuSet();

    public abstract int totalCpuCount();

    public static ExecInfo empty() {
        return builder().cpuSet(Sets.newHashSet())
                .start(LocalDateTime.parse("2000-01-01T00:00:00"))
                .stop(LocalDateTime.parse("2000-01-01T00:00:00"))
                .totalCpuCount(0)
                .metrics(Metrics.empty())
                .build();
    }

    public static Builder withNoMetrics() {
        return builder()
                .metrics(Metrics.empty());
    }

    public static Builder builder() {
        return new AutoValue_ExecInfo.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder metrics(Metrics metrics);

        public abstract Builder cpuSet(Set<Integer> cpuSet);

        public abstract Builder start(LocalDateTime start);

        public abstract Builder stop(LocalDateTime stop);

        public abstract Builder totalCpuCount(int totalCpuCount);

        public abstract ExecInfo build();
    }


}
