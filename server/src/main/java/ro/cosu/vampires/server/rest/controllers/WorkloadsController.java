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
import ro.cosu.vampires.server.rest.services.WorkloadsService;

import static spark.Spark.*;


public class WorkloadsController implements Controller {

    @Inject
    private WorkloadsService workloadsService;

    @Override
    public void loadRoutes() {

        post("/workloads", (request, response) ->{
            return workloadsService.createWorkload();
        }, new JsonTransformer());

        get("/workloads", (request, response) ->{
            return workloadsService.getWorkloads();
        }, new JsonTransformer());

        get("/workloads/:id", (request, response) ->{
            String id = request.params(":id");
            return workloadsService.getWorkload(id);
        }, new JsonTransformer());

        delete("/workloads/:id", (request, response) ->{
            String id = request.params(":id");
            workloadsService.delete(id);
            return  null;
        }, new JsonTransformer());

        post("/workloads/:id", (request, response) ->{
            String id = request.params(":id");
            return workloadsService.updateWorkload(id);
        }, new JsonTransformer());
    }
}
