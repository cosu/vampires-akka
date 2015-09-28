package ro.cosu.vampires.client;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.ExecResult;
import ro.cosu.vampires.server.Message;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.LinkedList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

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
            List<Double> loads = new LinkedList<>();

            log.info("executor " + message);

            collectLoads(loads);

            ExecResult execResult = Executor.execute(command);
            execResult.setLoads(loads);

            Message.Result result= new Message.Result(execResult, computation);
            getSender().tell(result, getSelf());
        }

        getContext().stop(getSelf());

    }

    private void collectLoads(List<Double> loads){

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);

        loads.add(osBean.getSystemLoadAverage());



        getContext().system().scheduler().schedule(scala.concurrent.duration.Duration.create(1, SECONDS),
                scala.concurrent.duration.Duration.create(1, SECONDS), () -> {
                    loads.add(osBean.getSystemLoadAverage());
                }, getContext().system().dispatcher());

    }

}
