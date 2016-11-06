/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ro.cosu.vampires.server.values.jobs.Job;


public class SamplingWithReplicationScheduler implements Scheduler {

    private static final Logger LOG = LoggerFactory.getLogger(SamplingWithReplicationScheduler.class);
    private final int backOffInterval;
    private final int numberOfJobsToSample;
    private long jobDeadline;
    private List<Job> jobList;
    private Random random = new Random(1111111111L);


    private Map<String, Scheduler> schedulerMap = new HashMap<>();

    /**
     * This scheduler takes a list of jobs and samples `numberOfJobs`. The jobs are also replicated across clients.
     * All clients will run the same jobs but in a different order.
     */
    public SamplingWithReplicationScheduler(List<Job> jobs, long jobDeadline, int backOffInterval, int numberOfJobsToSample) {
        jobList = new ArrayList<>(jobs);
        Collections.shuffle(jobList, random);
        jobList = jobList.subList(0, Math.min(numberOfJobsToSample, jobList.size()));
        this.numberOfJobsToSample = numberOfJobsToSample;
        this.jobDeadline = jobDeadline;
        this.backOffInterval = backOffInterval;
    }

    private List<Job> getJobsForScheduler() {
        Collections.shuffle(jobList, random);
        return new ArrayList<>(jobList);
    }

    @Override
    public Job getJob(String from) {

        Scheduler scheduler = schedulerMap.getOrDefault(from,
                new SimpleScheduler(getJobsForScheduler(), jobDeadline, TimeUnit.SECONDS, backOffInterval));

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
