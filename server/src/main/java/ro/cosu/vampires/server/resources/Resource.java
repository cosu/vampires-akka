package ro.cosu.vampires.server.resources;

public interface Resource {
    enum Status {
        CREATING,
        STARTING,
        IDLE,
        RUNNING,
        FAILED,
        STOPPED,
        UNKNOWN
    }

    public enum Type {
        SSH,
        LOCAL
    }

    void onStart();
    void onStop();

    ResourceDescription getDescription();
    Status getStatus();

}
