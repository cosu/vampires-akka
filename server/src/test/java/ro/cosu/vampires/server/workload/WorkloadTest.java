package ro.cosu.vampires.server.workload;

import org.junit.Test;

public class WorkloadTest {
    @Test
    public void testWorkloadBuilder() throws Exception {

        Workload build = Workload.builder()
                .computation(Computation.builder().command("test").id("10").build())
                .metrics(Metrics.empty())
                .result(Result.empty())
                .build();



    }
}
