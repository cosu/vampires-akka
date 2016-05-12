package ro.cosu.vampires.server.workload;

import org.junit.Test;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


public class AutoValueUtilTest {
    @Test
    public void buildFrom() throws Exception {
        WorkloadPayload foo = WorkloadPayload.builder().format("foo")
                .sequenceStop(0).sequenceStart(1).task("10").url("100").build();

        Workload.Builder builder = Workload.builder();

        Workload build = new AutoValueUtil<WorkloadPayload, Workload.Builder>() {
        }
                .builderFromPayload(foo, builder).build();

        assertThat(build.id(), not(isEmptyOrNullString()));

    }

}