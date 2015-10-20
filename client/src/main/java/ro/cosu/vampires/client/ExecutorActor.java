package ro.cosu.vampires.client;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.client.executors.CommandExecutor;
import ro.cosu.vampires.server.workload.Result;
import ro.cosu.vampires.server.workload.Workload;

public class ExecutorActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    public static Props props() {
        return Props.create(ExecutorActor.class);
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Workload) {
            Workload workload = (Workload) message;

            CommandExecutor executor = new CommandExecutor();

            Result result = executor.execute(workload.computation());



            getContext().actorSelection("/user/monitor").tell(workload.withResult(result), getSender());

        }

        getContext().stop(getSelf());

    }



}
