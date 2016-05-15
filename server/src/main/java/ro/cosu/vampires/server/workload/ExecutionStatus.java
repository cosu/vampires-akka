package ro.cosu.vampires.server.workload;


import com.google.auto.value.AutoValue;

import java.time.LocalDateTime;
import java.util.UUID;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class ExecutionStatus {


//    {
//        "id" : "041363e3-6551-4202-947a-9fa8dab240ec",
//            "status": "running",
//            "last_update_at": "2016-08-05T08:40:51.620Z",
//            "completed": 10,
//            "failed": 0,
//            "remaining": 5,
//            "total": 15
//    }

    public abstract String id();

    public abstract String status();

    public abstract LocalDateTime createdAt();

    public abstract LocalDateTime updatedAt();

    public abstract int failed();

    public abstract int remaining();

    public abstract int total();

    public abstract int completed();


    public static Builder builder() {
        return new AutoValue_ExecutionStatus.Builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public static  ExecutionStatus fromExecution(Execution execution) {
        return new AutoValue_ExecutionStatus.Builder()
                .createdAt(execution.createdAt())
                .updatedAt(LocalDateTime.now())
                .id(execution.id())
                .failed(0)
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder id(String id);

        public abstract Builder status(String status);

        public abstract Builder createdAt(LocalDateTime createdAt);

        public abstract Builder updatedAt(LocalDateTime createdAt);

        public abstract Builder total(int i);

        public abstract Builder completed(int i);

        public abstract Builder remaining(int i);

        public abstract Builder failed(int i);

        public abstract ExecutionStatus build();



    }

}
