/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.client.executors.fork;

import com.google.common.base.Joiner;
import com.google.inject.Inject;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import ro.cosu.vampires.client.allocation.CpuAllocator;
import ro.cosu.vampires.client.allocation.CpuSet;
import ro.cosu.vampires.server.values.jobs.Computation;
import ro.cosu.vampires.server.values.jobs.Result;
import ro.cosu.vampires.server.values.jobs.metrics.Trace;


public class ForkExecutor implements ro.cosu.vampires.client.executors.Executor {

    private static final int TIMEOUT_IN_MILLIS = 600000;
    private static final Logger LOG = LoggerFactory.getLogger(ForkExecutor.class);
    @Inject
    private CpuAllocator cpuAllocator;

    @Inject
    private Executor executor;

    private CpuSet cpuSet;

    private CommandLine getCommandLine(String command) {
        String newCommand = command;
        if (isNumaEnabled()) {
            final String cpus = Joiner.on(",").join(cpuSet.getCpuSet());
            newCommand = "numactl --physcpubind=" + cpus + " " + command;
        }

        LOG.info("executing {} with timeout {} minutes", newCommand, TIMEOUT_IN_MILLIS / 1000 / 60);
        return CommandLine.parse(newCommand);
    }

    @Override
    public Result execute(Computation computation) {
        acquireResources();

        CommandLine commandLine = getCommandLine(computation.command());

        CollectingLogOutputStream collectingLogOutputStream = configureExecutorAndGetLogOutputStream();

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ZonedDateTime start = ZonedDateTime.now(ZoneOffset.UTC);

        int exitCode = runAndGetExitCode(commandLine, resultHandler);

        ZonedDateTime stop = ZonedDateTime.now(ZoneOffset.UTC);

        releaseResources();

        return Result.builder()
                .duration(Duration.between(start, stop).toMillis())
                .exitCode(exitCode)
                .trace(getTrace(start, stop))
                .output(collectingLogOutputStream.getLines())
                .build();

    }

    private int runAndGetExitCode(CommandLine commandLine, DefaultExecuteResultHandler resultHandler) {
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

        return resultHandler.hasResult() ? resultHandler.getExitValue() : -1;
    }

    private CollectingLogOutputStream configureExecutorAndGetLogOutputStream() {
        CollectingLogOutputStream collectingLogOutputStream = new CollectingLogOutputStream();
        PumpStreamHandler handler = new PumpStreamHandler(collectingLogOutputStream);
        executor.setStreamHandler(handler);
        executor.setWatchdog(new ExecuteWatchdog(TIMEOUT_IN_MILLIS));
        executor.setWorkingDirectory(Paths.get("").toAbsolutePath().toFile());
        return collectingLogOutputStream;
    }

    @Override
    public void acquireResources() {
        cpuSet = cpuAllocator.acquireCpuSet().orElseThrow(() -> new RuntimeException("Unable to acquire cpus"));
        LOG.debug("Acquired cpus {}", cpuSet);
    }

    @Override
    public void releaseResources() {
        cpuAllocator.releaseCpuSets(cpuSet);
    }

    @Override
    public Type getType() {
        return Type.FORK;
    }

    private Trace getTrace(ZonedDateTime start, ZonedDateTime stop) {
        final Trace.Builder builder = Trace
                .withNoMetrics()
                .executor(getType().toString())
                .start(start)
                .stop(stop)
                .totalCpuCount(cpuAllocator.totalCpuCount());

        builder.cpuSet(cpuSet.getCpuSet());
        final Trace trace = builder.build();

        LOG.debug("{}", trace);
        return trace;
    }

    @Override
    public int getNCpu() {
        // this uses jvm info. We could use sigar to create this also but it adds a weird dependency here
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
