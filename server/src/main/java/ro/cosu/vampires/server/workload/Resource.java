package ro.cosu.vampires.server.workload;


import com.google.auto.value.AutoValue;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class Resource {

    //        "provider": "ec2",
    //        "type": "eu-west1.t2.nano",
//        "count": "5"
//        }


    public static Builder builder() {
        return new AutoValue_Resource.Builder();
    }

    public abstract String provider();

    public abstract String type();

    public abstract int count();

    public abstract Builder toBuilder();

    public Resource withCount(int i) {
        return toBuilder().count(i).build();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder count(int count);

        public abstract Builder type(String type);

        public abstract Builder provider(String provider);

        public abstract Resource build();
    }
}
