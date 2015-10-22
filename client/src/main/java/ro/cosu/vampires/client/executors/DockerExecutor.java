package ro.cosu.vampires.client.executors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Result;

import java.security.SecureRandom;

public class DockerExecutor implements Executor{

    final DockerClient dockerClient;

    public DockerExecutor(){

        DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder()
                .withUri("https://192.168.99.100:2376")
                .withDockerCertPath("/Users/cdumitru/.docker/machine/certs")
                .build();

        dockerClient = DockerClientBuilder.getInstance(config).build();






    }

    @Override
    public Result execute(Computation computation) {

        String containerName = "vampires-" + new SecureRandom().nextInt();

        CreateContainerResponse container = dockerClient.createContainerCmd("busybox").withCmd("echo", "1")
                .withName(containerName)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        int exitCode = dockerClient.waitContainerCmd(container.getId()).exec();

        try {
            String s = dockerClient.logContainerCmd(container.getId()).withStdOut().exec(new LogContainerTestCallback())
                    .awaitCompletion().toString();
            System.out.println(s);
        } catch (InterruptedException e) {
            //
        }

        dockerClient.removeContainerCmd(container.getId());
        dockerClient.waitContainerCmd(container.getId()).exec();





        return Result.empty();

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
