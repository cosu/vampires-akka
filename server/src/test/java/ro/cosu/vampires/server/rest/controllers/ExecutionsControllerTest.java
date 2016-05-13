package ro.cosu.vampires.server.rest.controllers;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import com.typesafe.config.ConfigFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.rest.services.ServicesTestModule;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.ExecutionPayload;
import ro.cosu.vampires.server.workload.Workload;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static ro.cosu.vampires.server.rest.controllers.Response.request;


public class ExecutionsControllerTest {

    private static ActorSystem system;
    private static Injector injector;
    private Gson gson = JsonTransformer.get().getGson();

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
        Spark.stop();
    }

    @BeforeClass
    public static void setUpClass() {
        Spark.init();
        system = ActorSystem.create("test", ConfigFactory.load("application-dev.conf"));
        Module testModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(ExecutionsController.class).asEagerSingleton();
                install(new ServicesTestModule(system));
            }
        };

        injector = Guice.createInjector(testModule);
        Spark.awaitInitialization();
    }

    protected ExecutionPayload getPayload() {
        return ExecutionPayload.builder()
                .workload(injector.getInstance(Workload.class).id())
                .configuration(injector.getInstance(Configuration.class).id())
                .type(ExecutionMode.FULL)
                .build();
    }

    @Test
    public void create() throws Exception {
        ExecutionPayload payload = getPayload();

        String toJson = gson.toJson(payload);
        Response res = request("POST", "/executions", toJson);

        assertThat(res.status, is(HTTP_CREATED));

        Execution execution = gson.fromJson(res.body, Execution.class);

        assertThat(execution.id(), not(isEmptyOrNullString()));
    }

    @Test
    public void list() throws Exception {
        create();
        Response res = request("GET", "/executions", "");
        Execution[] executions = gson.fromJson(res.body, Execution[].class);
        assertThat(executions.length, not(0));
    }

    @Test
    public void get() throws Exception {
        create();
        Response res = request("GET", "/executions", "");
        Execution[] executions = gson.fromJson(res.body, Execution[].class);
        res = request("GET", "/executions/" + executions[0].id(), "");
        assertThat(res.status, is(HTTP_OK));
    }

    @Test
    public void delete() throws Exception {
        create();
        Response res = request("GET", "/executions", "");
        Execution[] executions = gson.fromJson(res.body, Execution[].class);
        res = request("DELETE", "/executions/" + executions[0].id(), "");
        assertThat(res.status, is(HTTP_BAD_REQUEST));
    }
}
