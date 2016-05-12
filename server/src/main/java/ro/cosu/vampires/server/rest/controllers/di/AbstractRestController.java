package ro.cosu.vampires.server.rest.controllers.di;

import com.google.gson.Gson;
import com.google.inject.Inject;

import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.rest.services.di.Service;
import ro.cosu.vampires.server.workload.Id;
import spark.Route;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;

public abstract class AbstractRestController<T extends Id, P> implements IController {
    private final Class<T> valueType;
    private final Class<P> payloadType;

    @Inject
    private Service<T, P> service;

    AbstractRestController(Class<T> valueType, Class<P> payloadType) {
        this.valueType = valueType;
        this.payloadType = payloadType;
    }

    public Route list() {
        return (request, response) -> service.list();
    }

    public Route create() {
        return (request, response) -> {
            Gson gson = JsonTransformer.get().getGson();
            P fromJson = gson.fromJson(request.body(), payloadType);
            response.status(HTTP_CREATED);
            return service.create(fromJson);
        };
    }

    public Route get() {
        return (request, response) -> {
            String id = request.params(":id");
            return service.get(id);
        };
    }

    public Route delete() {
        return ((request, response) -> {
            String id = request.params(":id");
            service.delete(id);
            response.status(HTTP_NO_CONTENT);
            return null;
        });
    }

    public Route update() {
        return (request, response) -> {
            Gson gson = JsonTransformer.get().getGson();
            T fromJson = gson.fromJson(request.body(), valueType);
            response.status(HTTP_CREATED);
            return service.update(fromJson);
        };
    }
}
