/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.rest.controllers;

import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.rest.services.ConfigurationsService;
import ro.cosu.vampires.server.workload.Configuration;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;


public class ConfigurationsController implements Controller {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsController.class);

    @Inject
    private ConfigurationsService configurationsService;

    ConfigurationsController() {
        loadRoutes();
    }

    @Override
    public void loadRoutes() {

        Spark.get("/configurations", (request, response) ->{
            return configurationsService.getConfigurations();
        }, new JsonTransformer());

        post("/configurations", (request, response) ->{
            String body = request.body();
            try {
                JsonTransformer jsonTransformer = new JsonTransformer();
                Configuration configuration = jsonTransformer.getGson().fromJson(body, Configuration.class);
                if (configuration == null) {
                    response.status(HTTP_BAD_REQUEST);
                    return "";
                } else {
                    Configuration created = configurationsService.createConfiguration(configuration);
                    response.status(HTTP_CREATED);
                    return created;
                }
            } catch (JsonSyntaxException jse) {
                LOG.error("Bad request", jse);
                response.status(HTTP_BAD_REQUEST);
                return "";
            }

        }, new JsonTransformer());

        post("/configurations/:id", (request, response) ->{
            try {
                JsonTransformer jsonTransformer = new JsonTransformer();

                Configuration configuration = jsonTransformer.getGson().fromJson(request.body(), Configuration.class);

                Optional<Configuration> updateConfiguration = configurationsService.updateConfiguration(configuration);

                if (updateConfiguration.isPresent()) {
                    response.status(HTTP_CREATED);
                    return updateConfiguration.get();
                } else {
                    response.status(HTTP_BAD_REQUEST);
                    return "Update failed";
                }

            } catch (JsonSyntaxException jse) {
                LOG.error("Bad request", jse);
                response.status(HTTP_BAD_REQUEST);
                return "";
            }
        }, new JsonTransformer());

        delete("/configurations/:id", (request, response) ->{
            String id =  request.params(":id");
            Optional<Configuration> delete = configurationsService.deleteConfiguration(id);
            if (delete.isPresent()) {
                response.status(HTTP_NO_CONTENT);
            } else {
                response.status(HTTP_BAD_REQUEST);
            }
            return null;

        }, new JsonTransformer());

        get("/configurations/:id", (request, response) ->{
            String id = request.params("id");
            Optional<Configuration> configuration = configurationsService.getConfiguration(id);
            if (configuration.isPresent()) {
                response.status(HTTP_OK);
                return configuration.get();
            } else {
                response.status(HTTP_NOT_FOUND);
                return "";
            }
        }, new JsonTransformer());
    }
}
