package ro.cosu.vampires.client;

import org.apache.commons.exec.*;
import ro.cosu.vampires.server.ExecResult;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;


public class Executor {
    public static ExecResult execute (String command) {

        CommandLine cmd = CommandLine.parse(command);
        DefaultExecutor executor = new DefaultExecutor();

        CollectingLogOutputStream collectingLogOutputStream = new CollectingLogOutputStream();

        PumpStreamHandler handler = new PumpStreamHandler(collectingLogOutputStream);

        executor.setStreamHandler(handler);
        executor.setWatchdog(new ExecuteWatchdog(15000));

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

        return new ExecResult.Builder()
                .command(command)
                .exitCode(exitCode)
                .start(start)
                .duration(duration)
                .output(collectingLogOutputStream.getLines())
                .build();
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
