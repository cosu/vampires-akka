package ro.cosu.vampires.server.rest.services;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ro.cosu.vampires.server.actors.AbstractActorTest;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.ResourceDemand;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;


public class ConfigurationsServiceTest extends AbstractActorTest {


    private static Injector injector;
    private ConfigurationsService configurationsService;

    @BeforeClass
    public static void setUpClass() {
        ServicesModule controllersModule = new ServicesModule(getActorSystem());
        injector = Guice.createInjector(controllersModule);

    }

    @AfterClass
    public static void tearDown() {
        injector = null;
    }


    @Before
    public void setUp() throws Exception {
        configurationsService = injector.getInstance(ConfigurationsService.class);
    }


    @Test
    public void getConfigurations() throws Exception {
        createConfiguration();
        Collection<Configuration> configurations = configurationsService.getConfigurations();
        assertThat(configurations.size(), not(0));
    }

    @Test
    public void createConfiguration() throws Exception {

        ConfigurationPayload configuration = ConfigurationPayload.create("foo",
                ImmutableList.of(
                        ResourceDemand.builder().count(1).provider(Resource.ProviderType.MOCK).type("bar").build()
                ));


        configurationsService.createConfiguration(configuration);

        Collection<Configuration> configurations = configurationsService.getConfigurations();

        assertThat(configurations.size(), not(0));
    }

    @Test
    public void updateConfiguration() throws Exception {
        createConfiguration();

        Collection<Configuration> configurations = configurationsService.getConfigurations();

        assertThat(configurations.size(), not(0));

        Configuration configuration = configurations.iterator().next();

        List<ResourceDemand> resourceList = configuration.resources().stream().map(r -> r.withCount(42)).collect(Collectors.toList());
        Configuration updatedConfiguration = configuration.withResources(ImmutableList.copyOf(resourceList));
        configurationsService.updateConfiguration(updatedConfiguration);

        Optional<Configuration> configurationOptional = configurationsService.getConfiguration(configuration.id());

        assertThat(configurationOptional.isPresent(), is(true));

        assertThat(configurationOptional.get().resources().stream().allMatch(r -> r.count() == 42), is(true));
    }

    @Test
    public void deleteConfiguration() throws Exception {
        createConfiguration();

        Collection<Configuration> configurations = configurationsService.getConfigurations();

        assertThat(configurations.size(), not(0));

        Configuration configuration = configurations.iterator().next();

        configurationsService.deleteConfiguration(configuration.id());

        Optional<Configuration> configurationOptional = configurationsService.getConfiguration(configuration.id());

        assertThat(configurationOptional.isPresent(), is(false));

    }

    @Test
    public void getConfiguration() throws Exception {
        createConfiguration();

        Collection<Configuration> configurations = configurationsService.getConfigurations();

        assertThat(configurations.size(), not(0));

    }

}