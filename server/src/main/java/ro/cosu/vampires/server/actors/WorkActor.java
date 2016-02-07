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

    private int MAX_JOB_LENGTH = 30;
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
        int maxJobSeconds = settings.getMaxJobSeconds();
        pendingJobs = CacheBuilder.newBuilder().expireAfterWrite(maxJobSeconds, TimeUnit.SECONDS)
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
            Job job = (Job) message;
            resultActor.forward(job, getContext());
            log.info("Work result from {}. pending {} ", job.from(), pendingJobs.size());
            pendingJobs.invalidate(job.id());
            Object work = this.getNewWorkload(Optional.ofNullable(workQueue.poll()));
            getSender().tell(work, getSelf());
        } else if (message instanceof ResourceControl.Shutdown) {
            log.info("shutting down");
            resultActor.forward(message, getContext());
            getContext().stop(getSelf());
        } else {
            log.warning("unhandled message from {}", getSender());
            unhandled(message);
        }
    }

    private Object getNewWorkload(Optional<String> work) {
        Job job = Job.backoff();
        if (work.isPresent()) {
            Computation computation = Computation.builder().command(work.get()).build();
            log.debug("computation {}", computation);
            job = Job.empty().withComputation(computation);
            pendingJobs.put(job.id(), job);
        } else {
            log.debug("Backoff: {} ", getSender());
        }
        return job;
    }

}
