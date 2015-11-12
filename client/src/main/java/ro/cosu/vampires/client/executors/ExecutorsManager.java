package ro.cosu.vampires.client.executors;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;

public class ExecutorsManager {
    private final Map<Executor.Type, Executor> providers;

    @Inject
    public ExecutorsManager(Map<Executor.Type, Executor> resources) {
        this.providers= resources;
    }

    public Map<Executor.Type, Executor> getProviders() {
        return providers;
    }

    public Optional<Executor> getProvider(Executor.Type type){
        return Optional.ofNullable(providers.get(type));
    }
}
