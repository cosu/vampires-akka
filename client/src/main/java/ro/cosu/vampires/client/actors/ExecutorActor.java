package ro.cosu.vampires.client.actors;

import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.extension.ExecutorsExtension;
import ro.cosu.vampires.client.extension.ExecutorsExtensionImpl;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.Result;

public class ExecutorActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ExecutorsExtensionImpl executors = ExecutorsExtension.ExecutorsProvider.get(getContext().system());

    public static Props props() {
        return Props.create(ExecutorActor.class);
    }

    private final ActorSelection monitorActor;

    public ExecutorActor() {
        monitorActor = getContext().actorSelection("/user/monitor");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Job) {
            Job job = (Job) message;
            Executor executor = executors.getExecutor();
            Result result = executor.execute(job.computation());
            log.debug("done executing job {}", job.id());
            monitorActor.tell(job.withResult(result), getSender());
        }

        getContext().stop(getSelf());
    }


}
