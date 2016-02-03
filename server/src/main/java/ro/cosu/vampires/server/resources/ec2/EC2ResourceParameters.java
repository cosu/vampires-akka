package ro.cosu.vampires.server.resources.ec2;

import com.google.auto.value.AutoValue;
import com.typesafe.config.Config;
import ro.cosu.vampires.server.resources.Resource;

@AutoValue
public abstract class EC2ResourceParameters implements Resource.Parameters {

    public abstract String command();

    public abstract String imageId();

    public abstract String instanceType();

    public abstract String keyName();

    public abstract String region();

    public abstract String securityGroup();

    public abstract Resource.Type type();

    public static Builder builder() {
        return new AutoValue_EC2ResourceParameters.Builder().type(Resource.Type.EC2);
    }


    @AutoValue.Builder
    public abstract static class Builder implements Resource.Parameters.Builder {

        public abstract Builder command(String s);

        public abstract Builder imageId(String s);

        public abstract Builder instanceType(String s);

        public abstract Builder keyName(String s);

        public abstract Builder region(String s);

        public abstract Builder securityGroup(String s);

        ;

        public abstract Builder type(Resource.Type type);


        public Builder fromConfig(Config config) {

            this.command(config.getString("command"));
            this.instanceType(config.getString("instanceType"));
            this.imageId(config.getString("imageId"));
            this.keyName(config.getString("keyName"));
            this.region(config.getString("region"));
            this.securityGroup(config.getString("securityGroup"));
            return this;
        }

        public abstract EC2ResourceParameters build();
    }
}
