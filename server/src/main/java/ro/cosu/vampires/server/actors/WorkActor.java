package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.schedulers.Scheduler;

public class WorkActor extends UntypedActor {

    private final SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef resultActor;

    public static Props props() {
        return Props.create(WorkActor.class);
    }

    private Scheduler scheduler;

    @Override
    public void preStart() {
        int jobDeadlineSeconds = settings.getJobDeadline();
        log.debug("JobDeadline in seconds {}" , jobDeadlineSeconds);
        scheduler = settings.getScheduler();

        resultActor = getContext().actorOf(ResultActor.props(scheduler.getJobCount()), "resultActor");
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

    private void receiveJob(Job receivedJob) {
        if (!receivedJob.computation().id().equals(Computation.BACKOFF)
                && !receivedJob.computation().id().equals(Computation.EMPTY)) {
            scheduler.markDone(receivedJob);
            resultActor.forward(receivedJob, getContext());
        }

        Job work = scheduler.getJob(receivedJob.from());
        getSender().tell(work, getSelf());

        if (scheduler.isDone()) {
            stop();
        }
    }

    private void stop() {
        resultActor.tell(new ResourceControl.Shutdown(), getSelf());
        getContext().stop(getSelf());
    }
}
