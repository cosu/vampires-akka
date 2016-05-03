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

import com.google.inject.Inject;
import ro.cosu.vampires.server.rest.JsonTransformer;

import ro.cosu.vampires.server.rest.services.ConfigurationsService;
import spark.Spark;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;


public class ConfigurationsController implements Controller {

    @Inject
    private ConfigurationsService configurationsService;


    @Override
    public void loadRoutes() {

        Spark.get("/configurations", (request, response) ->{
            return configurationsService.getConfigurations();
        }, new JsonTransformer());

        post("/configurations", (request, response) ->{
            return configurationsService.createConfiguration();
        }, new JsonTransformer());

        post("/configurations/:id", (request, response) ->{
            String id = request.params(":id");

            return configurationsService.updateConfiguration(id);
        }, new JsonTransformer());

        delete("/configurations/:id", (request, response) ->{
            String id =  request.params(":id");
            return null;
        }, new JsonTransformer());

        get("/configurations/:id", (request, response) ->{
            String id = request.params("id");
            return configurationsService.getConfiguration(id);
        }, new JsonTransformer());
    }
}
