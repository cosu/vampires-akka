package ro.cosu.vampires.server.rest.controllers;


import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.rest.services.ExecutionsService;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionPayload;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static spark.Spark.get;
import static spark.Spark.post;

public class ExecutionController extends AbstractController {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionController.class);

    @Inject
    private ExecutionsService executionsService;

    @Override
    public void loadRoutes() {
        get("/executions", (request, response) -> {
            return executionsService.getExecutions();
        }, new JsonTransformer());

        get("/executions/:id", (request, response) -> {
            String id = request.params(":id");
            Optional<Execution> execution = executionsService.getExecution(id);
            if (execution.isPresent()) {
                response.status(HTTP_OK);
                return execution.get();
            } else {
                response.status(HTTP_NOT_FOUND);
                return "";
            }
        }, new JsonTransformer());


        post("/executions", (request, response) -> {
            String body = request.body();
            try {
                JsonTransformer jsonTransformer = new JsonTransformer();
                ExecutionPayload executionPayload = jsonTransformer.getGson().fromJson(body, ExecutionPayload.class);
                if (executionPayload == null) {
                    response.status(HTTP_BAD_REQUEST);
                    return "";
                } else {
                    Execution created = executionsService.create(executionPayload);
                    response.status(HTTP_CREATED);
                    return created;
                }
            } catch (JsonSyntaxException jse) {
                LOG.error("Bad request", jse);
                response.status(HTTP_BAD_REQUEST);
                return "";
            }

        }, new JsonTransformer());
    }
}
