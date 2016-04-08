package ro.cosu.vampires.client.executors.docker;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.DockerException;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.ProcessingException;

import ro.cosu.vampires.client.allocation.CpuAllocator;
import ro.cosu.vampires.client.allocation.CpuSet;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.executors.ExecutorMetricsCollector;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;
import ro.cosu.vampires.server.workload.Trace;

public class DockerExecutor implements Executor {

    private static final Logger LOG = LoggerFactory.getLogger(DockerExecutor.class);

    @Inject
    private DockerClient dockerClient;

    @Named("Config")
    @Inject
    private Config config;

    @Inject
    private CpuAllocator cpuAllocator;

    @Inject
    private ExecutorMetricsCollector executorMetricsCollector;

    private String containerId;

    private Optional<CpuSet> cpuSet;

    private void createContainer(String[] command) {
        final String containerName = "vampires-" + new SecureRandom().nextInt(Integer.MAX_VALUE);

        final String containerImage = config.getString("docker.image");

        final CreateContainerCmd createContainerCmd = dockerClient
                .createContainerCmd(containerImage).withCmd(command)
                .withName(containerName);

        if (cpuSet.isPresent()) {
            LOG.debug("docker cpuset {}", cpuSet);
            createContainerCmd.withCpuset(Joiner.on(",").join(cpuSet.get().getCpuSet()));
        }
        CreateContainerResponse container = createContainerCmd.exec();
        containerId = container.getId();
    }

    @Override
    public Result execute(Computation computation) {

        acquireResources();

        createContainer(computation.command().split(" "));

        LOG.info("running docker job {}", computation);

        LocalDateTime start = LocalDateTime.now();

        dockerClient.startContainerCmd(containerId).exec();

        executorMetricsCollector.startMonitoring(containerId);

        int exitCode = dockerClient.waitContainerCmd(containerId).exec();

        LocalDateTime stop = LocalDateTime.now();
        String output = "";
        try {
            output = getOutput();
        } catch (InterruptedException e) {
            exitCode = -1;
            LOG.error("docker get log error {}", e);
        }

        dockerClient.waitContainerCmd(containerId).exec();

        executorMetricsCollector.stopMonitoring();

        dockerClient.removeContainerCmd(containerId).exec();

        long duration = Duration.between(start, stop).toMillis();

        releaseResources();

        return Result.builder()
                .exitCode(exitCode)
                .trace(getTrace(start, stop))
                .duration(duration)
                .output(Collections.singletonList(output))
                .build();

    }

    private String getOutput() throws InterruptedException {
        return dockerClient.logContainerCmd(containerId)
                .withStdErr()
                .withStdOut()
                .exec(new DockerLogResultCallback())
                .awaitCompletion()
                .toString();
    }

    private Trace getTrace(LocalDateTime start, LocalDateTime stop) {
        final Trace.Builder builder = Trace.builder()
                .executorMetrics(executorMetricsCollector.getMetrics())
                .executor(getType().toString())
                .start(start)
                .stop(stop)
                .totalCpuCount(cpuAllocator.totalCpuCount());

        if (cpuSet.isPresent()) {
            builder.cpuSet(cpuSet.get().getCpuSet());
        }
        final Trace trace = builder.build();

        LOG.debug("Trace: {}", trace);
        return trace;
    }

    public int getNCpu() {
        final Info exec = dockerClient.infoCmd().exec();
        return exec.getNCPU();
    }

    @Override
    public boolean isAvailable() {

        try {
            dockerClient.pingCmd().exec();
            LOG.info("docker is available");

            return true;
        } catch (DockerException | ProcessingException e) {
            LOG.error("docker is not available: {}", e.getMessage());
            return false;
        }
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
        return Type.DOCKER;
    }

    public static class DockerLogResultCallback extends LogContainerResultCallback {
        protected final StringBuffer log = new StringBuffer();

        @Override
        public void onNext(Frame frame) {
            log.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
            super.onNext(frame);
        }

        @Override
        public String toString() {
            return log.toString();
        }
    }


}
