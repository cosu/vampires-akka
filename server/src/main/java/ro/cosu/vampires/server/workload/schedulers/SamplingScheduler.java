package ro.cosu.vampires.server.workload.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ro.cosu.vampires.server.workload.Job;


public class SamplingScheduler implements Scheduler {

    private static final Logger LOG = LoggerFactory.getLogger(SamplingScheduler.class);
    private final long jobDeadline;
    private final int backOffInterval;
    private List<Job> jobList;
    private Random random = new Random(1111111111L);


    private Map<String, Scheduler> schedulerMap = new HashMap<>();

    public SamplingScheduler(List<Job> jobs, long jobDeadline, int backOffInterval, int numberOfJobsToSample) {
        jobList = new ArrayList<>(jobs);
        Collections.shuffle(jobList, random);
        jobList = jobList.subList(0, Math.min(numberOfJobsToSample, jobs.size()));

        this.jobDeadline = jobDeadline;
        this.backOffInterval = backOffInterval;
    }

    @Override
    public Job getJob(String from) {
        Scheduler scheduler = schedulerMap.getOrDefault(from,
                new SimpleScheduler(jobList, jobDeadline, backOffInterval));

        schedulerMap.put(from, scheduler);

        return scheduler.getJob(from);
    }

    @Override
    public void markDone(Job job) {
        if (schedulerMap.containsKey(job.from()))
            schedulerMap.get(job.from()).markDone(job);
        else
            LOG.warn("received job result from unknown client {}", job.from());
    }

    @Override
    public boolean isDone() {
        return schedulerMap.values().stream().allMatch(Scheduler::isDone);
    }

    @Override
    public long getJobCount() {
        return jobList.size();
    }

}
