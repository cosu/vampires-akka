package ro.cosu.vampires.server.workload;


import com.google.auto.value.AutoValue;

import com.typesafe.config.Config;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class WorkloadPayload {

    public static Builder builder() {
        return new AutoValue_WorkloadPayload.Builder()
                .format("%s")
                .url("")
                .sequenceStart(0)
                .sequenceStop(0);

    }

    public static WorkloadPayload fromConfig(Config config) {
        String format = "";
        if (config.hasPath("format")) {
            format = config.getString("format");
        }

        String url = "";
        if (config.hasPath("url")) {
            url = config.getString("url");
        }

        int sequenceStart = config.getInt("sequenceStart");
        int sequenceStop = config.getInt("sequenceStop");
        String task = config.getString("task");

        return builder().format(format)
                .task(task)
                .url(url)
                .sequenceStart(sequenceStart)
                .sequenceStop(sequenceStop)
                .build();
    }

    public abstract int sequenceStart();

    public abstract int sequenceStop();

    public abstract String task();

    public abstract String format();

    public abstract String url();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder sequenceStart(int sequenceStrat);

        public abstract Builder sequenceStop(int sequenceStop);

        public abstract Builder task(String task);

        public abstract Builder format(String format);

        public abstract Builder url(String format);

        public abstract WorkloadPayload build();
    }

}
