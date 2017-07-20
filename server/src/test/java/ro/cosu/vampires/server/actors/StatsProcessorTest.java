package ro.cosu.vampires.server.actors;

import com.google.common.collect.Maps;

import org.junit.Test;

import ro.cosu.vampires.server.actors.execution.StatsProcessor;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.resources.mock.MockResourceParameters;
import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;
import ro.cosu.vampires.server.values.resources.ResourceDescription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


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
        MockResourceParameters mock = MockResourceParameters.builder().resourceDescription(
                ResourceDescription.builder().resourceType("mock").provider(Resource.ProviderType.MOCK).cost(10).build())
                .command("mock").build();
        ResourceInfo resourceInfo = ResourceInfo.create(mock, Resource.Status.CONNECTED);
        processor.process(resourceInfo);

        Job job = Job.empty().from(mock.id());
        processor.process(job);
        processor.flush();

        assertThat(processor.getStats().values().get("cost").value(), is(10.0));
        assertThat(processor.getStats().counters().get("resources").count(), is(1L));
    }


}