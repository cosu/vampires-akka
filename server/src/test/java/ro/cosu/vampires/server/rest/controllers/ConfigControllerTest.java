package ro.cosu.vampires.server.rest.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.TypeLiteral;

import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.rest.controllers.di.ConfigController;
import ro.cosu.vampires.server.rest.services.di.CService;
import ro.cosu.vampires.server.rest.services.di.Service;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.ResourceDemand;
import spark.Spark;
import spark.utils.IOUtils;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


public class ConfigControllerTest {

    @AfterClass
    public static void teardown() {
        Spark.stop();

    }

    @Test
    public void testInject() throws Exception {
        AbstractModule module = new AbstractModule() {

            @Override
            protected void configure() {

                bind(new TypeLiteral<Service<Configuration, ConfigurationPayload>>() {
                })
                        .to(new TypeLiteral<CService>() {
                        });
            }
        };

        ConfigController instance = Guice.createInjector(module).getInstance(ConfigController.class);

        Spark.post("/configurations", instance.create(), JsonTransformer.get());

        Spark.awaitInitialization();

        Gson gson = new JsonTransformer().getGson();
        Configuration configuration = Configuration.builder().description("foo")
                .resources(ImmutableList.of(
                        ResourceDemand.builder().count(1).provider(Resource.ProviderType.MOCK).type("bar").build()
                )).build();

        String toJson = gson.toJson(configuration);

        Response res = request("POST", "/configurations", toJson);

        System.out.println(res.body);
        assertThat(res.status, is(201));
        Configuration fromJson = gson.fromJson(res.body, Configuration.class);

        assertThat(fromJson.id(), not(isEmptyOrNullString()));


    }

    protected Response request(String method, String path, String payload) {
        try {
            URL url = new URL("http://localhost:4567" + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            if (payload.length() > 0) {
                connection.setRequestProperty("Content-ProviderType", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                OutputStream os = connection.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.close();

            }
            connection.connect();
            String body = IOUtils.toString(connection.getInputStream());

            return new Response(connection.getResponseCode(), body);
        } catch (IOException e) {
            return new Response(404, "");
        }
    }

    protected static class Response {
        public final int status;
        public final String body;

        private Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}