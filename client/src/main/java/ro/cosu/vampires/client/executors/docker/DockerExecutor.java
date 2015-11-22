package ro.cosu.vampires.client.executors.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.DockerException;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.client.allocation.CpuAllocator;
import ro.cosu.vampires.client.allocation.CpuSet;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;

import javax.ws.rs.ProcessingException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

public class DockerExecutor implements Executor {

    static final Logger LOG = LoggerFactory.getLogger(DockerExecutor.class);

    @Inject
    DockerClient dockerClient;

    @Named("Config")
    @Inject
    Config config;

    @Inject
    CpuAllocator cpuAllocator;

    @Override
    public Result execute(Computation computation) {

        LOG.info("running docker job {}", computation);
        String containerName = "vampires-" + Math.abs(new SecureRandom().nextInt());

        String containerImage = config.getString("docker.image");

        final CreateContainerCmd createContainerCmd = dockerClient
                .createContainerCmd(containerImage).withCmd(computation.command().split(" "))
                .withName(containerName);

        final Optional<CpuSet> cpuSet = cpuAllocator.acquireCpuSet();
        if (cpuSet.isPresent()) {
            final String cpus = Joiner.on(",").join(cpuSet.get().getCpuSet());
            createContainerCmd.withCpuset(cpus);
        }

        CreateContainerResponse container = createContainerCmd.exec();

        LocalDateTime start = LocalDateTime.now();

        dockerClient.startContainerCmd(container.getId()).exec();

        int exitCode = dockerClient.waitContainerCmd(container.getId()).exec();

        LocalDateTime stop = LocalDateTime.now();

        String output = "";
        try {
             output = dockerClient.logContainerCmd(container.getId())
                     .withStdErr()
                     .withStdOut().exec(new LogContainerTestCallback())
                     .awaitCompletion().toString();

        } catch (InterruptedException e) {
            exitCode = -1;
            LOG.error("docker get log error {}", e);
        }

        dockerClient.waitContainerCmd(container.getId()).exec();

        dockerClient.removeContainerCmd(container.getId()).exec();


        long duration = Duration.between(start, stop).toMillis();

        if (cpuSet.isPresent()) {
            cpuAllocator.releaseCpuSets(cpuSet.get());
        }

        return Result.builder()
                .exitCode(exitCode)
                .start(start)
                .stop(stop)
                .duration(duration)
                .output(Collections.singletonList(output))
                .build();

    }

    @Override
    public int getNCpu() {
        final Info exec = dockerClient.infoCmd().exec();
        return exec.getNCPU();
    }



    public static class LogContainerTestCallback extends LogContainerResultCallback {
        protected final StringBuffer log = new StringBuffer();

        @Override
        public void onNext(Frame frame) {
            log.append(new String(frame.getPayload()));
            super.onNext(frame);
        }

        @Override
        public String toString() {
            return log.toString();
        }
    }


    @Override
    public  boolean isAvailable() {

        try {
            dockerClient.pingCmd().exec();
            LOG.info("docker is available");

            return true;
        }
        catch (DockerException | ProcessingException e){
            LOG.error("docker is not available: {} ",e);
            return  false;
        }
    }
}