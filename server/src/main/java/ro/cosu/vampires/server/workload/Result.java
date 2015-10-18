package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@AutoValue
public abstract class  Result  implements Serializable {

    public abstract List<String> output();
    public abstract int exitCode();
    public abstract LocalDateTime start();
    public abstract LocalDateTime stop();
    public abstract  long duration ();


    public static Builder builder() {
        return new AutoValue_Result.Builder();
    }

    public static Result empty() {
        return new AutoValue_Result.Builder()
                .output(new LinkedList<>())
                .exitCode(-1)
                .start(LocalDateTime.MIN)
                .stop(LocalDateTime.MIN)
                .duration(0)
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder output(List<String> output);
        public abstract Builder exitCode(int exitCode);
        public abstract Builder start(LocalDateTime start);
        public abstract Builder stop(LocalDateTime stop);
        public abstract Builder duration(long duration);

        public abstract Result build();



    }
}
