package ro.cosu.vampires.server.resources;

import com.google.inject.Inject;

import java.util.Map;

public class ResourceManager {
    private final Map<String, IResource> resources;

    @Inject
    public ResourceManager(Map<String, IResource> resources) {
        this.resources = resources;
    }

    public Map<String, IResource> getResources() {
        return resources;
    }
}
