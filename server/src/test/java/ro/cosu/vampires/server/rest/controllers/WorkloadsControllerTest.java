package ro.cosu.vampires.server.rest.controllers;

import com.google.gson.Gson;

import org.junit.Test;

import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.workload.Workload;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


public class WorkloadsControllerTest extends AbstractControllerTest {

    @Test
    public void getWorkloads() throws Exception {
        Response res = request("GET", "/workloads", "");
        Gson gson = new JsonTransformer().getGson();
        Workload[] workloads = gson.fromJson(res.body, Workload[].class);
        assertThat(workloads.length, not(0));
    }

    @Test
    public void createWorkload() throws Exception {
        Gson gson = new JsonTransformer().getGson();
        Workload workload = Workload.builder().sequenceStart(0).sequenceStop(10).task("foo").format("%d").build();
        String toJson = gson.toJson(workload);

        Response res = request("POST", "/workloads", toJson);

        assertThat(res.status, is(201));
        Workload fromJson = gson.fromJson(res.body, Workload.class);

        assertThat(fromJson.id(), not(isEmptyOrNullString()));
    }

    @Test
    public void updateWorkload() throws Exception {
        createWorkload();
        Gson gson = new JsonTransformer().getGson();
        Response res = request("GET", "/workloads", "");
        assertThat(res.status, is(200));
        Workload[] workloads = gson.fromJson(res.body, Workload[].class);

        res = request("GET", "/workloads/" + workloads[0].id(), "");

        Workload fromJson = gson.fromJson(res.body, Workload.class);

        res = request("POST", "/workloads/" + fromJson.id(),
                gson.toJson(fromJson.update().sequenceStop(100).build()));

        assertThat(res.status, is(201));
        fromJson = gson.fromJson(res.body, Workload.class);

        assertThat(fromJson.sequenceStop(), is(100));
    }


    @Test
    public void getWorkload() throws Exception {
        Response res = request("GET", "/workloads", "");
        Gson gson = new JsonTransformer().getGson();
        assertThat(res.status, is(200));
        Workload[] workloads = gson.fromJson(res.body, Workload[].class);
        assertThat(workloads.length, not(0));

        res = request("GET", "/workloads/" + workloads[0].id(), "");
        assertThat(res.status, is(200));
        Workload workload = gson.fromJson(res.body, Workload.class);
        assertThat(workload.id(), is(workloads[0].id()));
    }

    @Test
    public void deleteWorkload() throws Exception {
        Response res = request("GET", "/workloads", "");
        Gson gson = new JsonTransformer().getGson();
        assertThat(res.status, is(200));
        Workload[] workloads = gson.fromJson(res.body, Workload[].class);
        assertThat(workloads.length, not(0));

        res = request("DELETE", "/workloads/" + workloads[0].id(), "");
        assertThat(res.status, is(204));
        assertThat(res.body, isEmptyOrNullString());
    }

    @Test
    public void getWorkload404() throws Exception {
        Response res = request("GET", "/workloads/foo", "");
        assertThat(res.status, is(404));
    }

}