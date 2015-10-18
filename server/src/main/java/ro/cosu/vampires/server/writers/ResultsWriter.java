package ro.cosu.vampires.server.writers;

import ro.cosu.vampires.server.workload.Workload;

public interface ResultsWriter{
    void writeResult(Workload result);
    void close();
}
