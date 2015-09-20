package ro.cosu.vampires.client;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.commons.exec.*;
import ro.cosu.vampires.server.ExecResult;
import ro.cosu.vampires.server.Message;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Executor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    public static Props props() {
        return Props.create(Executor.class);
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.Computation) {
            Message.Computation computation = (Message.Computation) message;
            String command = "sleep 4";
            List<Double> loads = new LinkedList<>();

            log.info("executor " + message);

            collectLoads(loads);

            CommandLine cmd = CommandLine.parse(command);
            DefaultExecutor executor = new DefaultExecutor();

            CollectingLogOutputStream collectingLogOutputStream = new CollectingLogOutputStream();
            PumpStreamHandler handler=new PumpStreamHandler(collectingLogOutputStream);

            executor.setStreamHandler(handler);
            executor.setWatchdog(new ExecuteWatchdog(15000));

            executor.setWorkingDirectory(Paths.get("").toAbsolutePath().toFile());

            LocalDateTime start = LocalDateTime.now();

            int exitCode = executor.execute(cmd);

            LocalDateTime stop = LocalDateTime.now();

            long duration = Duration.between(start, stop).toMillis();

            ExecResult execResult = new ExecResult.Builder()
                    .command(command)
                    .exitCode(exitCode)
                    .start(start)
                    .duration(duration)
                    .output(collectingLogOutputStream.getLines())
                    .loads(loads)
                    .build();

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

    private class CollectingLogOutputStream extends LogOutputStream {
        private final List<String> lines = new LinkedList<>();
        @Override protected void processLine(String line, int level) {
            lines.add(line);
        }
        public List<String> getLines() {
            return lines;
        }
    }
}
