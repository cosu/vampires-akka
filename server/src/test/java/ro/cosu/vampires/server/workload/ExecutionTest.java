package ro.cosu.vampires.server.workload;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class ExecutionTest {

    @Test
    public void build() throws Exception {

        Configuration configuration = Configuration.builder().resources(ImmutableList.of()).description("foo").build();
        Workload workload = Workload.builder().format("foo").sequenceStart(0).sequenceStop(10).task("bar").build();

        Execution build = Execution.builder().configuration(configuration).
                type(ExecutionMode.FULL).workload(workload).build();

        assertThat(build.status(), is("created"));
    }

}