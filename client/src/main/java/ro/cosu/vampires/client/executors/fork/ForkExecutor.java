package ro.cosu.vampires.client.executors.fork;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.client.allocation.CpuAllocator;
import ro.cosu.vampires.client.allocation.CpuSet;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.ExecInfo;
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

    private Optional<CpuSet> cpuSet;

    private CommandLine getCommandLine(String command){


        LOG.debug("cpuset {}", cpuSet);

        if (cpuSet.isPresent() && isNumaEnabled()) {
            final String cpus = Joiner.on(",").join(cpuSet.get().getCpuSet());
            command = "numactl --physcpubind=" + cpus + " " + command;
        }

        LOG.info("executing {} with timeout {} minutes", command, TIMEOUT_IN_MILIS / 1000 / 60);
        return  CommandLine.parse(command);
    }

    @Override
    public Result execute(Computation computation) {

        acquireResources();

        CommandLine commandLine = getCommandLine(computation.command());

        CollectingLogOutputStream collectingLogOutputStream = new CollectingLogOutputStream();
        PumpStreamHandler handler = new PumpStreamHandler(collectingLogOutputStream);
        executor.setStreamHandler(handler);
        executor.setWatchdog(new ExecuteWatchdog(TIMEOUT_IN_MILIS));
        executor.setWorkingDirectory(Paths.get("").toAbsolutePath().toFile());

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        LocalDateTime start = LocalDateTime.now();
        int exitCode;
        try {
            executor.execute(commandLine, resultHandler);
        } catch (IOException e) {
            LOG.error("failed to exec", resultHandler.getException());
        }

        try {
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            LOG.error("failed to exec", resultHandler.getException());
        }

        exitCode = resultHandler.hasResult()? resultHandler.getExitValue(): -1;

        //TODO take different action for failed commands so we can collect the output (stderr or java exception)


        LocalDateTime stop = LocalDateTime.now();

        long duration = Duration.between(start, stop).toMillis();

        releaseResources();
        return Result.builder()
                .duration(duration)
                .exitCode(exitCode)
                .execInfo(getExecInfo())
                .start(start)
                .stop(stop)
                .output(collectingLogOutputStream.getLines())
                .build();

    }

    @Override
    public void acquireResources() {
        cpuSet = cpuAllocator.acquireCpuSet();
    }

    @Override
    public void releaseResources() {
        cpuSet.ifPresent(c -> cpuAllocator.releaseCpuSets(c));
    }

    @Override
    public Type getType() {
        return Type.FORK;
    }

    private ExecInfo getExecInfo() {
        final ExecInfo.Builder builder = ExecInfo
                .withNoMetrics()
                .executor(Type.DOCKER.toString())
                .start(LocalDateTime.now())
                .stop(LocalDateTime.now())
                .totalCpuCount(cpuAllocator.totalCpuCount());

        if (cpuSet.isPresent()) {
            builder.cpuSet(cpuSet.get().getCpuSet());
        }
        final ExecInfo execInfo = builder.build();

        LOG.debug("{}", execInfo);
        return  execInfo;
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
