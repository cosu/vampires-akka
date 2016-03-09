package ro.cosu.vampires.server.workload.schedulers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.workload.Job;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class SimpleScheduler implements Scheduler {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleScheduler.class);

    protected final ConcurrentLinkedQueue<Job> workQueue = new ConcurrentLinkedQueue<>();

    private Cache<String, Job> pendingJobs;
    private List<Job> jobList;
    private final int backOffInterval;

    public SimpleScheduler(List<Job> jobList, long jobDeadline, int backOffInterval) {
        this.jobList = jobList;
        this.backOffInterval = backOffInterval;
        workQueue.addAll(jobList);
        pendingJobs = CacheBuilder.newBuilder().expireAfterWrite(jobDeadline, TimeUnit.SECONDS)
                .removalListener(notification -> {
                    if (!notification.wasEvicted()) {
                        return;
                    }
                    if (notification.getKey() != null && notification.getValue() != null
                            && notification.getKey() instanceof String) {
                        Job job = (Job) notification.getValue();
                        LOG.warn("Job {}: {} failed to return after {} . Re adding to queue",
                                job.id(), job.computation().command(), jobDeadline);
                        workQueue.add(job);
                    }
                }).build();
    }

    @Override
    public Job getJob(String from) {
        Optional<Job> work = Optional.ofNullable(workQueue.poll());
        Job job;
        if (work.isPresent()) {
            job = work.get();
            pendingJobs.put(job.id(), job);
        } else {
            job = Job.backoff(backOffInterval);
        }
        return job;
    }

    @Override
    public void markDone(Job receivedJob) {
        LOG.debug("Work result from {}. pending {} remaining {}",
                receivedJob.hostMetrics().metadata().get("host-hostname"),pendingJobs.size(), workQueue.size());
        pendingJobs.invalidate(receivedJob.id());
    }

    @Override
    public boolean isDone() {
        return pendingJobs.asMap().isEmpty() && workQueue.isEmpty();
    }

    @Override
    public long getJobCount() {
        return jobList.size();
    }


}
