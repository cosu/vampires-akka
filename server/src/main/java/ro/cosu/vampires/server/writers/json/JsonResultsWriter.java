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

package ro.cosu.vampires.server.writers.json;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.writers.ResultsWriter;

public class JsonResultsWriter implements ResultsWriter {
    private static final Logger LOG = LoggerFactory.getLogger(JsonResultsWriter.class);
    private final String uploadDirName;

    private List<Job> results = new LinkedList<>();
    private List<ClientInfo> clients = new LinkedList<>();


    public JsonResultsWriter(Config config) {
        Preconditions.checkArgument(config.hasPath("writers.json.dir"), "missing  config key writers.json.dir");
        uploadDirName = config.getString("writers.json.dir");
        boolean created = Paths.get(uploadDirName).toFile().mkdir();
        Preconditions.checkArgument(created, "could not create upload dir");
    }

    private Path getPath(String prefix) {
        LocalDateTime date = LocalDateTime.now();
        return Paths.get(uploadDirName, prefix + "-" + date.format
                (DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".json");

    }

    @Override
    public void addResult(Job result) {
//        Gson gson = ng GsonBuilder().setPrettyPrinting()
//                .registerTypeAdapter(LocalDateTime.class, ng LocalDateTimeSerializer())
//                .create();
//        WebsocketHandler.broadcastMessage("foo", gson.toJson(result));

        results.add(result);
    }

    @Override
    public void addClient(ClientInfo clientInfo) {
        clients.add(clientInfo);
    }

    public void close() {
        //write results to disk
        try (BufferedWriter fileWriter = Files.newWriter(getPath("results-all").toFile(), Charsets.UTF_8)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                    .setPrettyPrinting()
                    .create();

            AllResults allResults = AllResults.builder().results(results).clients(clients).build();

            gson.toJson(allResults, fileWriter);
            fileWriter.close();
            LOG.info("wrote results to {}", getPath("results-all").toAbsolutePath());
        } catch (IOException e) {
            LOG.error("Error writing results to file", e);
        }
    }


}
