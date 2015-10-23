package ro.cosu.vampires.client.executors;


import org.junit.Ignore;
import org.junit.Test;
import ro.cosu.vampires.server.workload.Computation;

public class DockerExecutorTest {

    @Test
    @Ignore
    public void testExecute() throws Exception {
        DockerExecutor dockerExecutor = new DockerExecutor();

        dockerExecutor.execute(Computation.builder().command("echo 1").build());

    }
}
