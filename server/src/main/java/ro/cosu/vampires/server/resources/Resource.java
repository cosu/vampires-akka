package ro.cosu.vampires.server.resources;

import java.util.concurrent.CompletableFuture;

public interface Resource {


    enum Status {
        CREATING,
        STARTING,
        IDLE,
        RUNNING,
        FAILED,
        STOPPING,
        STOPPED,
        UNKNOWN
    }

    public enum Type {
        SSH,
        LOCAL,
        DAS5
    }

    CompletableFuture<Resource> start();
    CompletableFuture<Resource> stop();

    void onStart() throws  Exception;
    void onStop() throws Exception;
    void onFail() throws Exception;

    ResourceDescription getDescription();
    Status getStatus();

}
