package ro.cosu.vampires.server.workload;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


public class ConfigurationTest {
    @Test
    public void builder() throws Exception {
        Configuration foo = Configuration.builder().description("foo")
                .resources(ImmutableList.of(
                )).build();
        assertThat(foo.id(), not(isEmptyOrNullString()));

    }

}