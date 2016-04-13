/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

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
import ro.cosu.vampires.server.workload.Trace;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


public class ForkExecutor implements ro.cosu.vampires.client.executors.Executor {

    public static final int TIMEOUT_IN_MILIS = 600000;
    private static final Logger LOG = LoggerFactory.getLogger(ForkExecutor.class);
    @Inject
    private CpuAllocator cpuAllocator;

    @Inject
    private Executor executor;

    private Optional<CpuSet> cpuSet;

    private CommandLine getCommandLine(String command) {

        LOG.debug("cpuset {}", cpuSet);
        String newCommand = command;
        if (cpuSet.isPresent() && isNumaEnabled()) {
            final String cpus = Joiner.on(",").join(cpuSet.get().getCpuSet());
            newCommand = "numactl --physcpubind=" + cpus + " " + command;
        }

        LOG.info("executing {} with timeout {} minutes", newCommand, TIMEOUT_IN_MILIS / 1000 / 60);
        return CommandLine.parse(newCommand);
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

        exitCode = resultHandler.hasResult() ? resultHandler.getExitValue() : -1;

        //TODO take different action for failed commands so we can collect the output (stderr or java exception)


        LocalDateTime stop = LocalDateTime.now();

        long duration = Duration.between(start, stop).toMillis();

        releaseResources();
        return Result.builder()
                .duration(duration)
                .exitCode(exitCode)
                .trace(getTrace(start, stop))

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

    private Trace getTrace(LocalDateTime start, LocalDateTime stop) {
        final Trace.Builder builder = Trace
                .withNoMetrics()
                .executor(getType().toString())
                .start(start)
                .stop(stop)
                .totalCpuCount(cpuAllocator.totalCpuCount());

        if (cpuSet.isPresent()) {
            builder.cpuSet(cpuSet.get().getCpuSet());
        }
        final Trace trace = builder.build();

        LOG.debug("{}", trace);
        return trace;
    }

    @Override
    public int getNCpu() {
        // this uses jvm info. We could use sigar to get this also but it adds a weird dependency here
        return Runtime.getRuntime().availableProcessors();
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
}
