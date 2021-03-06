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
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

import com.typesafe.config.ConfigFactory;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.rest.services.Service;
import ro.cosu.vampires.server.values.Id;
import ro.cosu.vampires.server.values.User;
import scala.concurrent.duration.Duration;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public abstract class AbstractControllerTest<T extends Id, P> {

    protected static ActorSystem actorSystem;


    protected static String url = "http://localhost:4567";
    private Gson gson = new JsonTransformer().getGson();

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(actorSystem, Duration.create("1 second"), true);
        actorSystem = null;

    }

    @BeforeClass
    public static void setUpClass() {
        Spark.init();
        actorSystem = ActorSystem.create("test", ConfigFactory.load("application-dev.conf"));
    }

    protected static HttpClient getHttpClient() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(User.admin().id(), User.admin().id()));
        return HttpClientBuilder.create()
                .setDefaultCredentialsProvider(credsProvider).build();
    }

    @Before
    public void setUp() throws Exception {
        Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(getModule());
                        bind(ExceptionMapper.class).asEagerSingleton();
                        bind(AuthenticationFilter.class).asEagerSingleton();
                    }
                });
        Spark.awaitInitialization();
        create();
    }

    protected abstract TypeLiteral<Service<T, P>> getTypeTokenService();

    protected abstract Module getModule();

    protected abstract P getPayload();

    protected abstract String getPath();

    @SuppressWarnings("unchecked")
    private List<T> getList(String json, Class clazz) throws Exception {
        Object[] array = (Object[]) java.lang.reflect.Array.newInstance(clazz, 1);
        array = gson.fromJson(json, array.getClass());
        List<T> list = new ArrayList<>();
        for (Object o : array) list.add((T) o);
        return list;
    }

    @SuppressWarnings("unchecked")
    private Class<?> getValueClass() throws ClassNotFoundException {
        Type typeValue = ((ParameterizedType) getTypeTokenService().getType()).getActualTypeArguments()[0];
        return Class.forName(typeValue.getTypeName());
    }

    private void checkResponseContainsItem(String response) throws ClassNotFoundException {
        Object fromJson = gson.fromJson(response, getValueClass());
        assertThat(fromJson instanceof Id, is(true));
        Id id = (Id) fromJson;
        assertThat(id.id(), not(isEmptyOrNullString()));
    }

    private T getFirstItem() throws Exception {

        HttpClient client = getHttpClient();
        HttpGet httpGet = new HttpGet(url + getPath());
        HttpResponse execute = client.execute(httpGet);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_OK));
        String entity = EntityUtils.toString(execute.getEntity());
        List<T> list = getList(entity, getValueClass());
        assertThat(list.size(), not(0));

        return list.get(0);
    }

    @Test
    public void getUnknown() throws Exception {
        HttpClient client = getHttpClient();
        String getUrl = url + Paths.get(getPath(), "foo");
        HttpGet httpGet = new HttpGet(getUrl);
        HttpResponse execute = client.execute(httpGet);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_NOT_FOUND));
    }

    @Test
    public void create() throws Exception {
        P payload = getPayload();

        String toJson = gson.toJson(payload);

        HttpPost httpPost = new HttpPost(url + getPath());
        httpPost.setEntity(new StringEntity(toJson));
        httpPost.setHeader("Content-type", "application/json");
        HttpClient client = getHttpClient();
        HttpResponse execute = client.execute(httpPost);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_CREATED));
        checkResponseContainsItem(EntityUtils.toString(execute.getEntity()));
    }

    @Test
    public void delete() throws Exception {
        Id item = getFirstItem();
        String deleteUrl = url + Paths.get(getPath(), item.id()).toString();

        HttpDelete httpDelete = new HttpDelete(deleteUrl);
        HttpClient httpClient = getHttpClient();
        HttpResponse execute = httpClient.execute(httpDelete);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_NO_CONTENT));
        assertThat(execute.getEntity(), nullValue());
    }

    @Test
    public void list() throws Exception {
        T firstItem = getFirstItem();
        assertThat(firstItem, not(nullValue()));
    }

    @Test
    public void update() throws Exception {
        Id firstItem = getFirstItem();
        String toJson = gson.toJson(firstItem);

        HttpPost httpPost = new HttpPost(url + Paths.get(getPath(), firstItem.id()).toString());
        httpPost.setEntity(new StringEntity(toJson));
        httpPost.setHeader("Content-type", "application/json");
        HttpClient client = getHttpClient();
        HttpResponse execute = client.execute(httpPost);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_CREATED));


        Id updatedItem = getFirstItem();
        assertThat(updatedItem.id(), is(firstItem.id()));
    }

    @Test
    public void updateFail() throws Exception {
        Id firstItem = getFirstItem();
        HttpPost httpPost = new HttpPost(url + Paths.get(getPath(), firstItem.id()).toString());
        httpPost.setEntity(new StringEntity(""));
        httpPost.setHeader("Content-type", "application/json");
        HttpClient client = getHttpClient();
        HttpResponse execute = client.execute(httpPost);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_BAD_REQUEST));
    }


    @Test
    public void unauthorized() throws Exception {
        HttpPost httpPost = new HttpPost(url + getPath());
        httpPost.setEntity(new StringEntity(""));
        httpPost.setHeader("Content-type", "application/json");
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse execute = client.execute(httpPost);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_UNAUTHORIZED));
    }
}
