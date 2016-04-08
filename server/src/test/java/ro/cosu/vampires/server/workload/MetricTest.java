package ro.cosu.vampires.server.workload;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class MetricTest {

    @Test
    public void testBuilder() throws Exception {
        Metric empty = Metric.empty();
        assertThat(empty.values().size(), is(0));
    }
}