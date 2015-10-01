package ro.cosu.vampires.server.resources;

public interface ResourceProvider {
    IResource create(String name);
}
