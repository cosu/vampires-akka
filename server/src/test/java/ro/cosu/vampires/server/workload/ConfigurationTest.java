package ro.cosu.vampires.server.workload;

import com.google.common.collect.ImmutableList;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import ro.cosu.vampires.server.resources.Resource;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


public class ConfigurationTest {
    @Test
    public void builder() throws Exception {
        Configuration foo = Configuration.builder().description("foo")
                .resources(ImmutableList.of(
                        ResourceDemand.builder().count(1).provider(Resource.ProviderType.MOCK).type("bar").build()
                )).build();
        assertThat(foo.id(), not(isEmptyOrNullString()));

        assertThat(foo.resources().size(), not(0));
    }

    @Test
    public void fromConfig() throws Exception {
        String startConfig = "{" +
                "description = foo\n" +
                "start  = [\n" +
                "    {\n" +
                "      provider = local\n" +
                "      type = local\n" +
                "      count = 1\n" +
                "    }\n" +
                "  ]" +
                "}";

        Config config = ConfigFactory.parseString(startConfig);
        Configuration configuration = Configuration.fromConfig(config);

        assertThat(configuration.resources().size(), is(1));
        assertThat(configuration.resources().get(0).provider(), is(Resource.ProviderType.LOCAL));
    }


}