package ro.cosu.vampires.server.util;

import ro.cosu.vampires.server.workload.Workload;

import java.util.List;

public interface ResultsWriter{
    void writeResults(List<Workload> results);

}
