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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.util.gson.AutoValueAdapterFactory;
import ro.cosu.vampires.server.util.gson.ImmutableListTypeAdapterFactory;
import ro.cosu.vampires.server.util.gson.ImmutableMapTypeAdapterFactory;
import ro.cosu.vampires.server.writers.json.AllResults;
import ro.cosu.vampires.server.writers.json.LocalDateTimeDeserializer;
import ro.cosu.vampires.server.writers.json.LocalDateTimeSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;


public class DataTest {

    private static final Logger LOG = LoggerFactory.getLogger(DataTest.class);

    private static AllResults loadFromJson(File jsonFile) throws FileNotFoundException {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .registerTypeAdapterFactory(new AutoValueAdapterFactory())
                .registerTypeAdapterFactory(new ImmutableMapTypeAdapterFactory())
                .registerTypeAdapter(ImmutableMap.class, ImmutableMapTypeAdapterFactory.newCreator())
                .registerTypeAdapterFactory(new ImmutableListTypeAdapterFactory())
                .create();

        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);

        AllResults response = gson.fromJson(inputStreamReader, AllResults.class);

        response.results().stream().forEach(job -> {

//            System.out.println(job.hostMetrics());
            job.hostMetrics().metrics().stream().forEach(metric -> {
                LOG.info("time: {}", metric.time());
                LOG.info("values: {}", metric.values());

            });

        });

        return response;

    }

    @Test
    @Ignore
    public void testName() throws Exception {

        String homeDir = System.getProperty("user.home");
        File file1 = Files.walk(Paths.get(homeDir), 1)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".json")).findFirst().get();

        LOG.info("file {}", file1);
        loadFromJson(file1);


    }

}
