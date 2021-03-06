package ro.cosu.vampires.server.values.rules;

import com.google.auto.value.AutoValue;

import java.time.Duration;
import java.util.List;

@AutoValue
public abstract class ScalingStrategy {

    /**
     * The name of the strategy
     * @return
     */
    public abstract String name();

    /**
     * Max number of instance
     * @return
     */
    public abstract int limit();

    /**
     * List of thresholds to be met to scale
     * @return
     */
    public abstract Threshold threshold();

    @AutoValue
    public static abstract class Threshold {
        public  abstract List<Condition>  conditions();
    }

    @AutoValue
    public static abstract class  Condition {

        public abstract String scope();

        public abstract double value();

        public abstract Duration timeWindow();

        public abstract String aggregationFunction();

        public abstract String metric();

        public abstract String operator();
    }


}
