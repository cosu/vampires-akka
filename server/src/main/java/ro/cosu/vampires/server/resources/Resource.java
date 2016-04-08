package ro.cosu.vampires.server.resources;

import com.typesafe.config.Config;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

public interface Resource {

    void connected();

    CompletableFuture<Resource> start();

    CompletableFuture<Resource> stop();

    void onStart() throws Exception;

    void onStop() throws Exception;

    void onFail() throws Exception;

    ResourceDescription description();

    Status status();

    default ResourceInfo info() {
        return ResourceInfo.create(description(), status());
    }

    enum Status {
        SLEEPING,
        STARTING,
        RUNNING,
        FAILED,
        STOPPING,
        STOPPED,
        UNKNOWN, CONNECTED,
    }

    enum Type {
        SSH,
        LOCAL,
        DAS5,
        EC2,
        MOCK
    }

    interface Parameters extends Serializable {
        Type type();

        String command();

        interface Builder {
            Builder fromConfig(Config config);

            Resource.Parameters build();
        }
    }

}
