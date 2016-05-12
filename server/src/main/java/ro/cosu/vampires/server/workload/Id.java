package ro.cosu.vampires.server.workload;

import java.time.LocalDateTime;

public interface Id {
    String id();

    LocalDateTime createdAt();

    LocalDateTime updatedAt();
}
