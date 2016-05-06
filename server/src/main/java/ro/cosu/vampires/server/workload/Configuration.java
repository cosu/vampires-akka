package ro.cosu.vampires.server.workload;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nullable;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class Configuration {

    //[
//        {
//        "id": "37ecfc16-f0fc-4c43-a915-bfef7ac6116b",
//        "created_at": "2016-08-05T08:40:51.620Z",
//        "lastupdate_at": "2016-08-05T08:40:51.620Z",
//        "description" : "my optional description",
//        "resources":
//        [
//        {
//        "provider": "ec2",
//        "type": "eu-west1.t2.micro",
//        "count": "5"
//        },
//        {
//        "provider": "ec2",
//        "type": "eu-west1.t2.nano",
//        "count": "5"
//        }
//        ],
//        "workload": "041363e3-6551-4202-947a-9fa8dab240ec",
//        "cost": 100
//        }
//        ]


    public static Builder builder() {
        return new AutoValue_Configuration.Builder().id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public abstract String id();

    @Nullable
    public abstract LocalDateTime createdAt();

    @Nullable
    public abstract LocalDateTime updatedAt();

    @Nullable
    public abstract String description();

    public abstract ImmutableList<Resource> resources();

    public abstract Builder toBuilder();

    public Builder update() {
        return toBuilder()
                .updatedAt(LocalDateTime.now());
    }


    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder id(String id);

        public abstract Builder resources(ImmutableList<Resource> resources);

        public abstract Builder description(String format);

        public abstract Builder createdAt(LocalDateTime createdAt);

        public abstract Builder updatedAt(LocalDateTime createdAt);

        public abstract Configuration build();
    }

}


