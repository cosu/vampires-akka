package ro.cosu.vampires.client.executors;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;

public class ExecutorsManager {
    private final Map<String, Executor> providers;

    @Inject
    public ExecutorsManager(Map<String, Executor> resources) {
        this.providers= resources;
    }

    public Map<String, Executor> getProviders() {
        return providers;
    }

    public Optional<Executor> getProvider(String type){
        return Optional.ofNullable(providers.get(type));
    }
}
