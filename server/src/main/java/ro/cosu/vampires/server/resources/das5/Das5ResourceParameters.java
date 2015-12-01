package ro.cosu.vampires.server.resources.das5;

import com.google.auto.value.AutoValue;
import com.typesafe.config.Config;
import ro.cosu.vampires.server.resources.Resource;

@AutoValue

public abstract  class Das5ResourceParameters implements Resource.Parameters {
    abstract String command();
    abstract String user();
    abstract String address();
    abstract String privateKey();
    abstract int port();
    public abstract Resource.Provider type();



    public static Builder builder() {
        return new AutoValue_Das5ResourceParameters.Builder().port(22).type(Resource.Provider.DAS5);
    }

    @AutoValue.Builder
    public abstract static class Builder implements Resource.Parameters.Builder{
        public abstract Builder command(String s);
        public abstract Builder user(String s);
        public abstract Builder address(String s);
        public abstract Builder privateKey(String s);
        public abstract Builder port(int i);
        abstract Builder type(Resource.Provider provider);


        public Builder fromConfig(Config config){
            this.command(config.getString("command"));
            this.user(config.getString("user"));
            this.address(config.getString("address"));
            this.privateKey(config.getString("privateKey"));
            if (config.hasPath("port")){
                this.port(config.getInt("port"));
            }
            return this;
        }

        public abstract Das5ResourceParameters build();

    }
}
