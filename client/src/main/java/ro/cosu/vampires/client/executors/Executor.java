package ro.cosu.vampires.client.executors;

import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;

public interface Executor {
    enum Type {
        FORK,
        DOCKER
    }
    Result execute(Computation computation);

    int getNCpu();

    default boolean isAvailable() { return  true;}

    void acquireResources();
    void releaseResources();

}
