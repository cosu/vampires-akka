package ro.cosu.vampires.server.resources;

import com.typesafe.config.Config;

import java.util.concurrent.CompletableFuture;

public interface Resource {

    interface  Parameters{
        Type type();
        interface Builder {
            Builder fromConfig(Config config);
            Resource.Parameters build();
        }
    }

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

    enum Type {
        SSH,
        LOCAL,
        DAS5,
        EC2
    }

    CompletableFuture<Resource> start();
    CompletableFuture<Resource> stop();

    void onStart() throws  Exception;
    void onStop() throws Exception;
    void onFail() throws Exception;

    ResourceDescription description();
    Status status();

    default ResourceInfo info() { return ResourceInfo.create(description(), status());}

}
