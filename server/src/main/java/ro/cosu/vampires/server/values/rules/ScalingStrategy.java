package ro.cosu.vampires.server.values.rules;

import com.google.auto.value.AutoValue;

import java.time.ZonedDateTime;

@AutoValue
public abstract class ScalingStrategy {
    public abstract int limit();
    public abstract Threshold threshold();

    @AutoValue
    public static abstract class Threshold {

    }

    public static abstract class  Condition {

        public abstract String scope();

        public abstract double value();

        public abstract ZonedDateTime startTime();

        public abstract ZonedDateTime endTime();

        public abstract String aggregationFunction();

        public abstract String metric();

        public abstract String operator();
    }


}
