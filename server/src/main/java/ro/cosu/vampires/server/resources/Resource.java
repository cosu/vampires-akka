package ro.cosu.vampires.server.resources;

import com.typesafe.config.Config;

import java.util.concurrent.CompletableFuture;

public interface Resource {

    interface  Parameters{
        Provider type();
        String command();
        interface Builder {
            Builder fromConfig(Config config);
            Resource.Parameters build();
        }
    }

    enum Status {
        CREATING,
        STARTING,
        RUNNING,
        FAILED,
        STOPPING,
        STOPPED,
        UNKNOWN
    }

    enum Provider {
        SSH,
        LOCAL,
        DAS5,
        EC2,
        MOCK
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
