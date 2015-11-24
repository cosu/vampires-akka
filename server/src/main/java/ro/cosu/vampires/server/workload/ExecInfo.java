package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;

import java.util.Set;


@AutoValue
public abstract class ExecInfo {

    public abstract Metrics metrics();

    public abstract Set<Integer> cpuSet();

    public static ExecInfo empty() {
        return builder().cpuSet(Sets.newHashSet())
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

        public abstract ExecInfo build();
    }


}
