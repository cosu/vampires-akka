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

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.inject.Inject;

import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Optional;

import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.rest.services.Service;
import ro.cosu.vampires.server.values.Id;
import ro.cosu.vampires.server.values.User;
import spark.Request;
import spark.Route;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;

public abstract class AbstractRestController<T extends Id, P> implements Controller {


    private final Class<P> payloadType;
    private final String path;

    @Inject
    private Service<T, P> service;

    AbstractRestController(Class<P> payloadType, String path) {
        this.payloadType = payloadType;
        this.path = Paths.get(path).toAbsolutePath().toString();
        loadRoutes();
    }


    private User getUser(Request request) {
        Preconditions.checkNotNull(request.session().attribute("user"), "invalid user provided");
        return User.create(request.session().attribute("user"));
    }

    protected abstract Logger getLogger();

    private void loadRoutes() {
        String idPath = Paths.get(path, "/:id").toString();
        Spark.get(path, list(), JsonTransformer.get());
        Spark.get(idPath, get(), JsonTransformer.get());
        Spark.delete(idPath, delete(), JsonTransformer.get());
        Spark.post(path, create(), JsonTransformer.get());
        Spark.post(idPath, update(), JsonTransformer.get());
    }

    @Override
    public Route list() {
        return (request, response) -> {
            logRequest("list", request);
            return service.list(getUser(request));
        };
    }

    public Route create() {
        return (request, response) -> {
            logRequest("create", request);
            User user = getUser(request);
            Gson gson = JsonTransformer.get().getGson();
            P fromJson = gson.fromJson(request.body(), payloadType);
            response.status(HTTP_CREATED);
            return service.create(fromJson, user);
        };
    }

    @Override
    public Route get() {
        return (request, response) -> {
            logRequest("get", request);
            String id = request.params(":id");
            Optional optional = service.get(id, getUser(request));
            if (optional.isPresent()) {
                return optional.get();
            } else {
                throw new FileNotFoundException("could not find resource " + id);
            }
        };
    }

    @Override
    public Route delete() {
        return ((request, response) -> {
            logRequest("delete", request);
            String id = request.params(":id");
            Preconditions.checkNotNull(id, "id missing");
            service.delete(id, getUser(request));
            response.status(HTTP_NO_CONTENT);
            return null;
        });
    }

    @Override
    public Route update() {
        return (request, response) -> {
            logRequest("update", request);
            Gson gson = JsonTransformer.get().getGson();
            P fromJson = gson.fromJson(request.body(), payloadType);
            response.status(HTTP_CREATED);
            return service.update(fromJson, getUser(request));
        };
    }

    private void logRequest(String type, Request request) {
        getLogger().debug("User:{} action:{} url: {}", request.session().attribute("user"), type, request.url());
    }
}
