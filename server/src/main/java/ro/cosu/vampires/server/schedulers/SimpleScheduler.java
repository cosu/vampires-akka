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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import ro.cosu.vampires.server.values.jobs.Job;

public class SimpleScheduler implements Scheduler {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleScheduler.class);

    protected final ConcurrentLinkedQueue<Job> workQueue = new ConcurrentLinkedQueue<>();
    private final int backOffInterval;
    private Cache<String, Job> pendingJobs;
    private List<Job> jobList;

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
                receivedJob.hostMetrics().metadata().get("host-hostname"), pendingJobs.size(), workQueue.size());
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
