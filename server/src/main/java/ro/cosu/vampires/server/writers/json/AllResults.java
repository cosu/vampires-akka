package ro.cosu.vampires.server.writers.json;

import com.google.auto.value.AutoValue;

import java.util.List;

import ro.cosu.vampires.server.util.gson.AutoGson;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Job;

@AutoValue
@AutoGson
public abstract class AllResults {
    public static Builder builder() {
        return new AutoValue_AllResults.Builder();
    }

    public abstract List<Job> results();

    public abstract List<ClientInfo> clients();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder results(List<Job> results);

        public abstract Builder clients(List<ClientInfo> clients);

        public abstract AllResults build();
    }


}
