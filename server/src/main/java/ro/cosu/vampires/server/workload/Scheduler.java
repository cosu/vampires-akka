package ro.cosu.vampires.server.workload;


public interface Scheduler {
    Job getJob(String from);
    void markDone(Job job);

    boolean isDone();
}
