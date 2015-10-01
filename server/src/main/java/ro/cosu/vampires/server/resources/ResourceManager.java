package ro.cosu.vampires.server.resources;

import com.google.inject.Inject;

import java.util.Map;

public class ResourceManager {
    private final Map<String, ResourceProvider> resources;

    @Inject
    public ResourceManager(Map<String, ResourceProvider> resources) {
        this.resources = resources;
    }

    public Map<String, ResourceProvider> getResources() {
        return resources;
    }
}
