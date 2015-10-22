package ro.cosu.vampires.server.workload;

import org.junit.Test;

public class JobTest {
    @Test
    public void testWorkloadBuilder() throws Exception {

        Job build = Job.builder()
                .computation(Computation.builder().command("test").id("10").build())
                .metrics(Metrics.empty())
                .result(Result.empty())
                .build();



    }
}
