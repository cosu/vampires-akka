package ro.cosu.vampires.server.resources.mock;

import com.google.auto.value.AutoValue;
import com.typesafe.config.Config;
import ro.cosu.vampires.server.resources.Resource;


@AutoValue
public abstract class MockResourceParameters implements Resource.Parameters{

    public abstract String command();
    public abstract Resource.Provider type();

    public static Builder builder() {
        return new AutoValue_MockResourceParameters.Builder().type(Resource.Provider.MOCK);
    }
    @AutoValue.Builder
    public abstract static class Builder implements  Resource.Parameters.Builder{
        public Builder fromConfig(Config config){
            this.command(config.getString("command"));
            return  this;
        }

        abstract Builder type(Resource.Provider provider);

        public abstract Builder command(String command);
        public abstract MockResourceParameters build();

    }
}
