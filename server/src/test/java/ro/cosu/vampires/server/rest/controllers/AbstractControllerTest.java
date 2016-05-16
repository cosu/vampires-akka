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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.rest.services.Service;
import ro.cosu.vampires.server.workload.Id;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static ro.cosu.vampires.server.rest.controllers.Response.request;

public abstract class AbstractControllerTest<T extends Id, P> {
    private Gson gson = new JsonTransformer().getGson();

    @AfterClass
    public static void teardown() {
        Spark.stop();
    }

    @BeforeClass
    public static void setUpClass() {
        Spark.init();
    }

    @Before
    public void setUp() throws Exception {
        Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(getModule());
                        bind(ExceptionMapper.class).asEagerSingleton();
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

    private void checkResponseContainsItem(Response res) throws ClassNotFoundException {
        Object fromJson = gson.fromJson(res.body, getValueClass());
        assertThat(fromJson instanceof Id, is(true));
        Id id = (Id) fromJson;
        assertThat(id.id(), not(isEmptyOrNullString()));
    }

    private T getFirstItem() throws Exception {
        Response res = request("GET", getPath(), "");
        assertThat(res.status, is(HTTP_OK));

        List<T> list = getList(res.body, getValueClass());
        assertThat(list.size(), not(0));

        return list.get(0);
    }

    @Test
    public void create() throws Exception {
        P payload = getPayload();

        String toJson = gson.toJson(payload);
        Response res = request("POST", getPath(), toJson);

        assertThat(res.status, is(HTTP_CREATED));
        checkResponseContainsItem(res);
    }


    @Test
    public void delete() throws Exception {
        Id item = getFirstItem();
        Response res = request("DELETE", Paths.get(getPath(), item.id()).toString(), "");
        assertThat(res.status, is(HTTP_NO_CONTENT));
        assertThat(res.body, isEmptyOrNullString());
    }

    @Test
    public void list() throws Exception {
        Response res = request("GET", getPath(), "");
        assertThat(res.status, is(HTTP_OK));
        List array = gson.fromJson(res.body, List.class);
        assertThat(array.size(), not(0));
    }

    @Test
    public void update() throws Exception {
        Id firstItem = getFirstItem();
        String toJson = gson.toJson(firstItem);
        Response res = request("POST", Paths.get(getPath(), firstItem.id()).toString(), toJson);
        assertThat(res.status, is(HTTP_CREATED));
        Id updatedItem = getFirstItem();
        assertThat(updatedItem.id(), is(firstItem.id()));
    }

    @Test
    public void updateFail() throws Exception {
        Id firstItem = getFirstItem();

        // update with a empty body
        Response res = request("POST", Paths.get(getPath(), firstItem.id()).toString(), "");

        assertThat(res.status, is(HTTP_BAD_REQUEST));

        Id updatedItem = getFirstItem();
        assertThat(updatedItem.id(), is(firstItem.id()));
    }
}
