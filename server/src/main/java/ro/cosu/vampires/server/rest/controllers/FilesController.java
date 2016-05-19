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

import com.google.inject.Inject;

import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import ro.cosu.vampires.server.rest.JsonTransformer;
import spark.Route;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_CREATED;

public class FilesController {

    private static final Logger LOG = LoggerFactory.getLogger(FilesController.class);
    private static long maxFileSize = 100000000;  // the maximum size allowed for uploaded files
    private static long maxRequestSize = 100000000;  // the maximum size allowed for multipart/form-data requests
    private static int fileSizeThreshold = 1024;  // the size threshold after which files will be written to disk
    private File uploadDir;

    @Inject
    FilesController(Config config) {
        uploadDir = new File(Paths.get(config.getString("uploadDir"), "/vampires").toUri());
        uploadDir.mkdir();

        LOG.debug("Upload dir {}", uploadDir);
        Spark.post("/upload", upload(), JsonTransformer.get());
        Spark.get("/upload", list(), JsonTransformer.get());

    }


    public Route upload() {
        return (request, response) -> {

            Path uploads = Files.createTempDirectory("uploads");

            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
                    uploads.toAbsolutePath().toString(), maxFileSize, maxRequestSize,
                    fileSizeThreshold);

            request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
            String fileName = request.raw().getPart("file").getSubmittedFileName();

            LOG.debug("upload file: {}", fileName);

            Part uploadedFilePart = request.raw().getPart("file");

            Path filePath = Paths.get(uploadDir.getAbsolutePath(), fileName);

            try (final InputStream input = uploadedFilePart.getInputStream()) {
                Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            response.status(HTTP_CREATED);
            return "OK";
        };
    }


    public Route list() {
        return (request, response) -> {

            return Files.walk(Paths.get(uploadDir.getAbsolutePath())).filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .map(Path::toFile)
                    .collect(Collectors.toMap(File::getName, File::length));
        };

    }
}
