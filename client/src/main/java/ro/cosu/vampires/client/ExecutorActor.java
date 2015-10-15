package ro.cosu.vampires.client;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.ExecResult;
import ro.cosu.vampires.server.Message;

public class ExecutorActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    public static Props props() {
        return Props.create(ExecutorActor.class);
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.Computation) {
            Message.Computation computation = (Message.Computation) message;
            String command = computation.getCommand();

            log.info("executor " + message);

            ExecResult execResult = Executor.execute(command);

            Message.Result result= new Message.Result(execResult, computation);

            getContext().actorSelection("/user/monitor").tell(result, getSender());

        }

        getContext().stop(getSelf());

    }



}
