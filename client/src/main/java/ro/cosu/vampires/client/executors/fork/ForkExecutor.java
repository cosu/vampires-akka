package ro.cosu.vampires.client.executors.fork;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.client.allocation.CpuAllocator;
import ro.cosu.vampires.client.allocation.CpuSet;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


public class ForkExecutor implements ro.cosu.vampires.client.executors.Executor {

    static final Logger LOG = LoggerFactory.getLogger(ForkExecutor.class);
    public static final int TIMEOUT_IN_MILIS = 600000;

    @Inject
    CpuAllocator cpuAllocator;

    @Inject
    Executor executor;

    @Override
    public Result execute(Computation computation) {

        String command = computation.command();

        final Optional<CpuSet> cpuSet = cpuAllocator.acquireCpuSet();
        LOG.info("cpuset {}", cpuSet);
        if (cpuSet.isPresent() && isNumaEnabled()) {
            final String cpus = Joiner.on(",").join(cpuSet.get().getCpuSet());
            command = "numactl --physcpubind=" + cpus + " " + command;
        }

        LOG.info("executing {} with timeout {} minutes", command, TIMEOUT_IN_MILIS / 1000 / 60);
        CommandLine cmd = CommandLine.parse(command);

        CollectingLogOutputStream collectingLogOutputStream = new CollectingLogOutputStream();

        PumpStreamHandler handler = new PumpStreamHandler(collectingLogOutputStream);

        executor.setStreamHandler(handler);
        executor.setWatchdog(new ExecuteWatchdog(TIMEOUT_IN_MILIS));

        executor.setWorkingDirectory(Paths.get("").toAbsolutePath().toFile());

        LocalDateTime start = LocalDateTime.now();

        int exitCode;
        try {
            exitCode = executor.execute(cmd);
        } catch (ExecuteException e) {
            LOG.warn("{}", e);
            exitCode = e.getExitValue();
        } catch (IOException e) {
            LOG.warn("{}", e);
            exitCode = -1;
        }

        LocalDateTime stop = LocalDateTime.now();

        long duration = Duration.between(start, stop).toMillis();

        if (cpuSet.isPresent()) {
            cpuAllocator.releaseCpuSets(cpuSet.get());
        }

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

        @Override
        protected void processLine(String line, int level) {
            lines.add(line);
        }

        public List<String> getLines() {
            return lines;
        }
    }

    private boolean isNumaEnabled() {
        int exitCode;
        try {
            executor.setStreamHandler(new PumpStreamHandler(new CollectingLogOutputStream()));
            exitCode = executor.execute(CommandLine.parse("numactl --hardware"));

        } catch (IOException e) {
            exitCode = -1;
        }
        return (exitCode == 0);

    }
}
