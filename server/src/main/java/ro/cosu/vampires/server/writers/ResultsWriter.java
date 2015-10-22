package ro.cosu.vampires.server.writers;

import ro.cosu.vampires.server.workload.Job;

public interface ResultsWriter{
    void writeResult(Job result);
    void close();
}
