/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.rest.controllers;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import com.typesafe.config.ConfigFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.rest.services.ServicesTestModule;
import ro.cosu.vampires.server.values.resources.Configuration;
import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.ExecutionMode;
import ro.cosu.vampires.server.values.jobs.ExecutionPayload;
import ro.cosu.vampires.server.values.jobs.Workload;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static ro.cosu.vampires.server.rest.controllers.AbstractControllerTest.getHttpClient;
import static ro.cosu.vampires.server.rest.controllers.AbstractControllerTest.url;


public class ExecutionsControllerTest {

    private static ActorSystem system;
    private static Injector injector;
    private Gson gson = JsonTransformer.get().getGson();

    private String controllerUrl = "/executions";

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
                bind(AuthenticationFilter.class).asEagerSingleton();
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

        HttpPost httpPost = new HttpPost(url + controllerUrl);
        httpPost.setEntity(new StringEntity(toJson));
        httpPost.setHeader("Content-type", "application/json");

        HttpClient client = getHttpClient();

        HttpResponse execute = client.execute(httpPost);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_CREATED));

        Execution execution = gson.fromJson(EntityUtils.toString(execute.getEntity()), Execution.class);

        assertThat(execution.id(), not(isEmptyOrNullString()));
    }

    @Test
    public void list() throws Exception {
        create();

        HttpClient client = getHttpClient();
        HttpGet httpGet = new HttpGet(url + controllerUrl);
        HttpResponse execute = client.execute(httpGet);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_OK));

        Execution[] executions = gson.fromJson(EntityUtils.toString(execute.getEntity()), Execution[].class);
        assertThat(executions.length, not(0));

    }

    @Test
    public void get() throws Exception {
        create();

        HttpClient client = getHttpClient();
        HttpGet httpGet = new HttpGet(url + controllerUrl);
        HttpResponse execute = client.execute(httpGet);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_OK));

        Execution[] executions = gson.fromJson(EntityUtils.toString(execute.getEntity()), Execution[].class);
        assertThat(executions.length, not(0));

        httpGet = new HttpGet(url + controllerUrl + "/" + executions[0].id());
        execute = client.execute(httpGet);

        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_OK));
    }

    @Test
    public void delete() throws Exception {
        create();

        HttpGet httpGet = new HttpGet(url + controllerUrl);
        HttpClient client = getHttpClient();

        HttpResponse execute = client.execute(httpGet);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_OK));

        Execution[] executions = gson.fromJson(EntityUtils.toString(execute.getEntity()), Execution[].class);

        String deleteUrl = url + controllerUrl + "/" + executions[0].id();

        HttpDelete httpDelete = new HttpDelete(deleteUrl);

        HttpClient httpClient = getHttpClient();

        execute = httpClient.execute(httpDelete);

        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_NO_CONTENT));

        assertThat(execute.getEntity(), nullValue());

    }
}
