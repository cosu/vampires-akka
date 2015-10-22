package ro.cosu.vampires.server.writers.mongo;

import org.junit.Test;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Metrics;
import ro.cosu.vampires.server.workload.Result;
import ro.cosu.vampires.server.workload.Job;

public class MongoWriterTest {

    @Test
//    @Ignore
    public void testWriteResult() throws Exception {
        MongoWriter writer = new MongoWriter();
        Job job = Job.builder()
                .computation(Computation.builder().command("test").id("10").build())
                .metrics(Metrics.empty())
                .result(Result.empty())
                .build();

        writer.writeResult(job);
    }
}
