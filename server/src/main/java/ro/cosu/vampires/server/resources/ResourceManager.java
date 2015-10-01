package ro.cosu.vampires.server.resources;

import com.google.inject.Inject;

import java.util.Map;

public class ResourceManager {
    private final Map<Resource.Type, ResourceProvider> providers;

    @Inject
    public ResourceManager(Map<Resource.Type, ResourceProvider> resources) {
        this.providers= resources;
    }

    public Map<Resource.Type, ResourceProvider> getProviders() {
        return providers;
    }
}
