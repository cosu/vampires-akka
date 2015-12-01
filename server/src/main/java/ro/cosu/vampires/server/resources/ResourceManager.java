package ro.cosu.vampires.server.resources;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;

public class ResourceManager {
    private final Map<Resource.Provider, ResourceProvider> providers;

    @Inject
    public ResourceManager(Map<Resource.Provider, ResourceProvider> resources) {
        this.providers= resources;
    }

    public Map<Resource.Provider, ResourceProvider> getProviders() {
        return providers;
    }

    public Optional<ResourceProvider > getProvider(Resource.Provider provider){
        return Optional.ofNullable(providers.get(provider));
    }
}
