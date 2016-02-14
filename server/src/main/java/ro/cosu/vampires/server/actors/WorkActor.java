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

    private final ConcurrentLinkedQueue<String> workQueue = new ConcurrentLinkedQueue<>();

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
                        log.warning("Job {}: {} failed to return. Re adding to queue", job.id(), job.computation().command());
                        workQueue.add(job.computation().command());
                    }
                }).build();
        initq();
        resultActor = getContext().actorOf(ResultActor.props(workQueue.size()), "resultActor");

    }

    private void initq() {
        log.info("adding work init");
        settings.getWorkload().stream().forEach(workQueue::add);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Job) {
            receiveJob((Job) message);
        } else if (message instanceof ResourceControl.Shutdown) {
            log.info("shutting down");
            resultActor.forward(message, getContext());
            getContext().stop(getSelf());
        } else {
            log.warning("unhandled message from {}", getSender());
            unhandled(message);
        }
    }

    private void receiveJob(Job job) {
        if (!Computation.backoff().equals(job.computation()) &&
                !Computation.empty().equals(job.computation())) {
            log.info("Work result from {}. pending {} ", job.metrics().metadata().get("host-hostname"), pendingJobs.size());
            pendingJobs.invalidate(job.id());
            resultActor.forward(job, getContext());

        }
        Job work = getNewWork(Optional.ofNullable(workQueue.poll()));
        getSender().tell(work, getSelf());
    }

    private Job getNewWork(Optional<String> work) {
        Job job = Job.backoff();
        if (work.isPresent()) {
            Computation computation = Computation.builder().command(work.get()).build();
            job = Job.empty().withComputation(computation);
            pendingJobs.put(job.id(), job);
        } else {
            log.debug("Backoff: {} ", getSender());
        }
        return job;
    }

}
