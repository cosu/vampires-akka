package ro.cosu.vampires.server.writers;

import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Job;

public interface ResultsWriter{
    void addResult(Job result);
    void addClient(ClientInfo clientInfo);
    void close();


}
