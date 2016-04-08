package ro.cosu.vampires.client.executors;

import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;

public interface Executor {
    Result execute(Computation computation);

    int getNCpu();

    default boolean isAvailable() {
        return true;
    }

    void acquireResources();

    void releaseResources();

    Type getType();

    enum Type {
        FORK,
        DOCKER
    }

}
