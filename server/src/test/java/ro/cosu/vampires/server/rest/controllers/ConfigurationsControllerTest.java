package ro.cosu.vampires.server.rest.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ResourceDemand;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


public class ConfigurationsControllerTest extends AbstractControllerTest {
    private ConfigurationsController configurationsController;

    @Before
    public void setUp() {
        configurationsController = injector.getInstance(ConfigurationsController.class);
    }

    @Test
    public void getConfigurations() throws Exception {
        Response res = request("GET", "/configurations", "");
        Gson gson = new JsonTransformer().getGson();
        Configuration[] Configurations = gson.fromJson(res.body, Configuration[].class);
        assertThat(Configurations.length, not(0));
    }

    @Test
    public void createConfiguration() throws Exception {
        Gson gson = new JsonTransformer().getGson();
        Configuration configuration = Configuration.builder().description("foo")
                .resources(ImmutableList.of(
                        ResourceDemand.builder().count(1).provider(Resource.ProviderType.MOCK).type("bar").build()
                )).build();

        String toJson = gson.toJson(configuration);

        Response res = request("POST", "/configurations", toJson);

        assertThat(res.status, is(201));
        Configuration fromJson = gson.fromJson(res.body, Configuration.class);

        assertThat(fromJson.id(), not(isEmptyOrNullString()));
    }

    @Test
    public void updateConfiguration() throws Exception {
        createConfiguration();
        Gson gson = new JsonTransformer().getGson();
        Response res = request("GET", "/configurations", "");
        assertThat(res.status, is(200));
        Configuration[] Configurations = gson.fromJson(res.body, Configuration[].class);

        res = request("GET", "/configurations/" + Configurations[0].id(), "");

        Configuration fromJson = gson.fromJson(res.body, Configuration.class);

        List<ResourceDemand> resourceList = fromJson.resources().stream().map(r -> r.withCount(42)).collect(Collectors.toList());

        Configuration updatedConfiguration = fromJson.withResources(ImmutableList.copyOf(resourceList));

        res = request("POST", "/configurations/" + fromJson.id(),
                gson.toJson(updatedConfiguration));

        assertThat(res.status, is(201));
        fromJson = gson.fromJson(res.body, Configuration.class);

        assertThat(fromJson.resources().stream()
                .allMatch(r -> r.count() == 42), is(true));
    }


    @Test
    public void getConfiguration() throws Exception {
        createConfiguration();
        Response res = request("GET", "/configurations", "");
        Gson gson = new JsonTransformer().getGson();
        assertThat(res.status, is(200));
        Configuration[] Configurations = gson.fromJson(res.body, Configuration[].class);
        assertThat(Configurations.length, not(0));

        res = request("GET", "/configurations/" + Configurations[0].id(), "");
        assertThat(res.status, is(200));
        Configuration Configuration = gson.fromJson(res.body, Configuration.class);
        assertThat(Configuration.id(), is(Configurations[0].id()));
    }

    @Test
    public void deleteConfiguration() throws Exception {
        createConfiguration();
        Response res = request("GET", "/configurations", "");
        Gson gson = new JsonTransformer().getGson();
        assertThat(res.status, is(200));
        Configuration[] Configurations = gson.fromJson(res.body, Configuration[].class);
        assertThat(Configurations.length, not(0));

        res = request("DELETE", "/configurations/" + Configurations[0].id(), "");
        assertThat(res.status, is(204));
        assertThat(res.body, isEmptyOrNullString());
    }

    @Test
    public void getConfiguration404() throws Exception {
        Response res = request("GET", "/configurations/foo", "");
        assertThat(res.status, is(404));
    }

}