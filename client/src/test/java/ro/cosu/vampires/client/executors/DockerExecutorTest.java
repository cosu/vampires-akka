package ro.cosu.vampires.client.executors;

import org.junit.Test;
import ro.cosu.vampires.server.workload.Computation;

public class DockerExecutorTest {

    @Test
    public void testExecute() throws Exception {
        DockerExecutor dockerExecutor = new DockerExecutor();

        dockerExecutor.execute(Computation.builder().command("echo 1").build());

    }
}
