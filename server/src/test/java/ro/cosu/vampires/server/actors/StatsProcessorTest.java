package ro.cosu.vampires.server.actors;

import com.google.common.collect.Maps;

import org.junit.Test;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.mock.MockResourceParameters;
import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;

public class StatsProcessorTest {
    @Test
    public void process() throws Exception {
        final ClientInfo clientInfo = ClientInfo.builder()
                .executors(Maps.newHashMap())
                .metrics(Metrics.empty())
                .id("foo")
                .build();
        StatsProcessor processor = new StatsProcessor();
        processor.process(clientInfo);
        MockResourceParameters mock = MockResourceParameters.builder().instanceType("mock")
                .command("mock").build();
        ResourceInfo resourceInfo = ResourceInfo.create(mock, Resource.Status.CONNECTED);
        processor.process(resourceInfo);

        Job job = Job.empty().from(mock.id());
        processor.process(job);
    }

    @Test
    public void process1() throws Exception {

    }

    @Test
    public void process2() throws Exception {

    }

}