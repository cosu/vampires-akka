package ro.cosu.vampires.server.writers.json;

import com.google.auto.value.AutoValue;
import ro.cosu.vampires.server.util.gson.AutoGson;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Job;

import java.util.List;

@AutoValue
@AutoGson
public abstract  class AllResults {
    public abstract List<Job> results();
    public abstract List<ClientInfo> clients();


    public static Builder builder() {
        return new AutoValue_AllResults.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder results(List<Job> results);

        public abstract Builder clients(List<ClientInfo> clients);

        public abstract AllResults build();
    }




}
