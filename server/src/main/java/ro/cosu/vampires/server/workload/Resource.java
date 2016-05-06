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


    public abstract String description();

    public abstract String type();

    public abstract int count();


    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder count(int count);

        public abstract Builder type(String type);

        public abstract Builder description(String description);

        public abstract Resource build();
    }
}
