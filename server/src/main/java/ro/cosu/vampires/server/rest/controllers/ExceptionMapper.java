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


import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.HashMap;

import ro.cosu.vampires.server.rest.JsonTransformer;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

public class ExceptionMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionMapper.class);

    ExceptionMapper() {
        Spark.exception(RuntimeException.class, JsonResponse.internalServerError());
        Spark.exception(NullPointerException.class, JsonResponse.badRequest());
        Spark.exception(FileNotFoundException.class, JsonResponse.fileNotFound());
    }


    private static class JsonResponse implements ExceptionHandler {
        private final int status;

        JsonResponse(int status) {
            this.status = status;
        }

        private static JsonResponse badRequest() {
            return new JsonResponse(HTTP_BAD_REQUEST);
        }

        private static JsonResponse fileNotFound() {
            return new JsonResponse(HTTP_NOT_FOUND);
        }

        private static JsonResponse internalServerError() {
            return new JsonResponse(HTTP_INTERNAL_ERROR);
        }

        @Override
        public void handle(Exception exception, Request request, Response response) {
            LOG.error("Error for client {} path {} body {}", request.ip(), request.url(), request.body(), exception);
            response.status(status);
            HashMap<Object, Object> map = Maps.newHashMap();
            map.put("error", exception.getMessage());
            response.body(JsonTransformer.get().getGson().toJson(map));
        }
    }
}
