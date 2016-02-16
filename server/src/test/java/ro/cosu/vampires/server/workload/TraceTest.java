package ro.cosu.vampires.server.workload;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TraceTest {

    @Test
    public void testWithNoMetrics() {
        Trace trace = Trace.withNoMetrics()
                .start(LocalDateTime.now())
                .stop(LocalDateTime.now())
                .cpuSet(Sets.newSet(1))
                .totalCpuCount(1)
                .executor("foo")
                .build();
        assertThat(trace.executorMetrics(), is(Metrics.empty()));
    }

}