package ro.cosu.vampires.client.executors;

import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;


public class CommandExecutor implements Executor {

    static final Logger LOG = LoggerFactory.getLogger(CommandExecutor.class);
    public static final int TIMEOUT_IN_MILIS = 600000;


    @Override
    public  Result execute(Computation computation) {

        String command = computation.command();

        LOG.info("executing {} with timeout {} minutes", command, TIMEOUT_IN_MILIS/1000/60);
        CommandLine cmd = CommandLine.parse(command);
        DefaultExecutor executor = new DefaultExecutor();

        CollectingLogOutputStream collectingLogOutputStream = new CollectingLogOutputStream();

        PumpStreamHandler handler = new PumpStreamHandler(collectingLogOutputStream);

        executor.setStreamHandler(handler);
        executor.setWatchdog(new ExecuteWatchdog(TIMEOUT_IN_MILIS));

        executor.setWorkingDirectory(Paths.get("").toAbsolutePath().toFile());

        LocalDateTime start = LocalDateTime.now();

        int exitCode;
        try {
            exitCode =executor.execute(cmd);
        }
        catch (ExecuteException e){
            exitCode = e.getExitValue();
        } catch (IOException e) {
            exitCode = -1;
        }

        LocalDateTime stop = LocalDateTime.now();

        long duration = Duration.between(start, stop).toMillis();

        return Result.builder()
                .duration(duration)
                .exitCode(exitCode)
                .start(start)
                .stop(stop)
                .output(collectingLogOutputStream.getLines())
                .build();

    }

    @Override
    public int getNCpu() {
        // this uses jvm info. We could use sigar to get this also but it adds a weird dependency here
        return Runtime.getRuntime().availableProcessors();
    }


    private static class CollectingLogOutputStream extends LogOutputStream {
        private final List<String> lines = new LinkedList<>();
        @Override protected void processLine(String line, int level) {
            lines.add(line);
        }
        public List<String> getLines() {
            return lines;
        }
    }
}
