package ro.cosu.vampires.server.resources.ssh;

import com.google.auto.value.AutoValue;
import com.typesafe.config.Config;
import ro.cosu.vampires.server.resources.Resource;

@AutoValue

public abstract class SshResourceParameters implements Resource.Parameters {

    public abstract String command();

    public abstract String user();

    public abstract String address();

    public abstract String privateKey();

    public abstract int port();

    public abstract Resource.Type type();


    public static Builder builder() {
        return new AutoValue_SshResourceParameters.Builder().port(22).type(Resource.Type.SSH);
    }

    @AutoValue.Builder
    public abstract static class Builder implements Resource.Parameters.Builder {

        public Builder fromConfig(Config config) {
            this.command(config.getString("command"));
            this.user(config.getString("user"));
            this.address(config.getString("address"));
            this.privateKey(config.getString("privateKey"));
            if (config.hasPath("port")) {
                this.port(config.getInt("port"));
            }
            return this;
        }


        public abstract Builder type(Resource.Type type);

        public abstract Builder command(String s);

        public abstract Builder user(String s);

        public abstract Builder address(String s);

        public abstract Builder privateKey(String s);

        public abstract Builder port(int i);

        public abstract SshResourceParameters build();

    }
}
