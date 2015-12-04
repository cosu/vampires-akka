package ro.cosu.vampires.server.resources.local;

import com.google.auto.value.AutoValue;
import com.typesafe.config.Config;
import ro.cosu.vampires.server.resources.Resource;

@AutoValue
public abstract class LocalResourceParameters implements Resource.Parameters {


    public abstract String command();

    public abstract Resource.Provider type();

    public static Builder builder() {
        return new AutoValue_LocalResourceParameters.Builder().type(Resource.Provider.LOCAL);
    }

    @AutoValue.Builder
    public abstract static class Builder implements Resource.Parameters.Builder {
        public Builder fromConfig(Config config) {
            this.command(config.getString("command"));
            return this;
        }

        public abstract Builder type(Resource.Provider provider);


        public abstract Builder command(String command);

        public abstract LocalResourceParameters build();

    }

}
