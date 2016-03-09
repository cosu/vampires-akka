package ro.cosu.vampires.server.workload.schedulers;


import ro.cosu.vampires.server.workload.Job;

public interface Scheduler {
    Job getJob(String from);
    void markDone(Job job);
    boolean isDone();
    long getJobCount();
}
