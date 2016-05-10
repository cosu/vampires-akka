package ro.cosu.vampires.server.rest.controllers;

import com.google.gson.Gson;

import org.junit.Test;

import ro.cosu.vampires.server.rest.JsonTransformer;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


public class ProvidersControllerTest extends AbstractControllerTest {

    @Test
    public void getProviders() throws Exception {
        Response res = request("GET", "/providers", "");
        Gson gson = new JsonTransformer().getGson();
        String[] workloads = gson.fromJson(res.body, String[].class);
        assertThat(workloads.length, not(0));
    }
}