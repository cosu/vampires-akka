package ro.cosu.vampires.server.resources;

public interface IResource {

    void start();
    void stop();
    default ResourceStatus getStatus(){
        return this.getStatus();
    }
}
