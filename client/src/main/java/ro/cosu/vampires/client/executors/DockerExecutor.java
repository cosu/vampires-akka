package ro.cosu.vampires.client.executors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

public class DockerExecutor implements Executor{

    static final Logger LOG = LoggerFactory.getLogger(DockerExecutor.class);

    @Inject
    DockerClient dockerClient;




    @Override
    public Result execute(Computation computation) {

        String containerName = "vampires-" + new SecureRandom().nextInt();

        CreateContainerResponse container = dockerClient.createContainerCmd("busybox").withCmd("echo", "1")
                .withName(containerName)
                .exec();

        LocalDateTime start = LocalDateTime.now();

        dockerClient.startContainerCmd(container.getId()).exec();

        LocalDateTime stop = LocalDateTime.now();

        int exitCode = dockerClient.waitContainerCmd(container.getId()).exec();
        String output ="";
        try {
             output = dockerClient.logContainerCmd(container.getId()).withStdOut().exec(new LogContainerTestCallback())
                    .awaitCompletion().toString();

        } catch (InterruptedException e) {
            exitCode = -1;
            LOG.error("docker get log error {}", e);
        }

        dockerClient.removeContainerCmd(container.getId());
        dockerClient.waitContainerCmd(container.getId()).exec();

        long duration = Duration.between(start, stop).toMillis();


        return Result.builder()
                .exitCode(exitCode)
                .start(start)
                .stop(stop)
                .duration(duration)
                .output(Collections.singletonList(output))
                .build();

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


}
