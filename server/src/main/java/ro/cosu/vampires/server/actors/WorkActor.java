package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Job;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class WorkActor extends UntypedActor {

    private final SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());

    private final ConcurrentLinkedQueue<Job> workQueue = new ConcurrentLinkedQueue<>();

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef resultActor;

    private Cache<String, Job> pendingJobs;

    public static Props props() {
        return Props.create(WorkActor.class);
    }

    @Override
    public void preStart() {
        int jobDeadlineSeconds = settings.getJobDeadline();
        log.debug("JobDeadline in seconds {}" , jobDeadlineSeconds);
        pendingJobs = CacheBuilder.newBuilder().expireAfterWrite(jobDeadlineSeconds, TimeUnit.SECONDS)
                .removalListener(notification -> {
                    if (!notification.wasEvicted()) {
                        return;
                    }
                    if (notification.getKey() != null && notification.getValue() != null
                            && notification.getKey() instanceof String) {
                        Job job = (Job) notification.getValue();
                        log.warning("Job {}: {} failed to return after {} . Re adding to queue",
                                job.id(), job.computation().command(), jobDeadlineSeconds);
                        workQueue.add(job);
                    }
                }).build();
        initq();
        resultActor = getContext().actorOf(ResultActor.props(workQueue.size()), "resultActor");

    }

    private void initq() {
        workQueue.addAll(settings.getWorkload());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Job) {
            receiveJob((Job) message);
        } else {
            log.warning("unhandled message from {}", getSender());
            unhandled(message);
        }
    }

    private void receiveJob(Job job) {
        if (!job.computation().id().equals(Computation.BACKOFF) && !job.computation().id().equals(Computation.EMPTY)) {
            log.debug("Work result from {}. pending {} remaining {}", job.hostMetrics().metadata().get("host-hostname"),
                    pendingJobs.size(), workQueue.size());
            pendingJobs.invalidate(job.id());
            resultActor.forward(job, getContext());
        }
        Job work = getNewWork(Optional.ofNullable(workQueue.poll()));
        getSender().tell(work, getSelf());
    }

    private Job getNewWork(Optional<Job> work) {
        Job job = Job.backoff(settings.getBackoffInterval());
        if (work.isPresent()) {
            job = work.get();
            pendingJobs.put(job.id(), job);
        } else {
            log.debug("Backoff: {} ", getSender());
        }
        if (pendingJobs.size() == 0)  {
            resultActor.tell(new ResourceControl.Shutdown(), getSelf());
            getContext().stop(getSelf());
        }
        return job;
    }

}
