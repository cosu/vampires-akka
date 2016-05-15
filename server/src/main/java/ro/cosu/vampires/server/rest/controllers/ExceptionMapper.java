package ro.cosu.vampires.server.rest.controllers;


import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import ro.cosu.vampires.server.rest.JsonTransformer;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class ExceptionMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionMapper.class);

    ExceptionMapper() {
        Spark.exception(RuntimeException.class, (exception, request, response) -> {
            LOG.error("exception", exception);
            response.status(HTTP_BAD_REQUEST);
            HashMap<Object, Object> map = Maps.newHashMap();
            map.put("error", exception.getMessage());
            response.body(JsonTransformer.get().getGson().toJson(map));
        });

    }
}
