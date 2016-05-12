package ro.cosu.vampires.server.rest.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import org.junit.Test;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.ExecutionPayload;
import ro.cosu.vampires.server.workload.ResourceDemand;
import ro.cosu.vampires.server.workload.Workload;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class ExecutionControllerTest extends AbstractControllerTest {

    private Workload createWorkload() {
        Gson gson = new JsonTransformer().getGson();
        Workload workload = Workload.builder().sequenceStart(0).sequenceStop(10).task("foo").format("%d").build();
        String toJson = gson.toJson(workload);
        Response res = request("POST", "/workloads", toJson);
        return gson.fromJson(res.body, Workload.class);
    }

    private Configuration createConfiguration() {
        Gson gson = new JsonTransformer().getGson();
        Configuration configuration = Configuration.builder().description("foo")
                .resources(ImmutableList.of(
                        ResourceDemand.builder().count(1).provider(Resource.ProviderType.MOCK).type("bar").build()
                )).build();

        String toJson = gson.toJson(configuration);
        Response res = request("POST", "/configurations", toJson);

        return gson.fromJson(res.body, Configuration.class);
    }

    @Test
    public void getExecution404() throws Exception {
        Response res = request("GET", "/executions/foo", "");
        assertThat(res.status, is(404));
    }


    @Test
    public void create() throws Exception {
        Configuration configuration = createConfiguration();
        Workload workload = createWorkload();

        ExecutionPayload executionPayload = ExecutionPayload.builder()
                .workload(workload.id())
                .configuration(configuration.id())
                .type(ExecutionMode.FULL)
                .build();

        Gson gson = new JsonTransformer().getGson();

        Response res = request("POST", "/executions", gson.toJson(executionPayload));

        assertThat(res.status, is(201));

        Execution execution = gson.fromJson(res.body, Execution.class);

        assertThat(execution.id(), not(isEmptyOrNullString()));

    }

    @Test
    public void getExecutions() throws Exception {
        create();
        Response res = request("GET", "/executions", "");
        Gson gson = new JsonTransformer().getGson();
        Execution[] Configurations = gson.fromJson(res.body, Execution[].class);
        assertThat(Configurations.length, not(0));
    }
}