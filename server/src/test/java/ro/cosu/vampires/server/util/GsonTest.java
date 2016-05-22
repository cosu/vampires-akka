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

package ro.cosu.vampires.server.util;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import ro.cosu.vampires.server.util.gson.AutoValueAdapterFactory;
import ro.cosu.vampires.server.util.gson.ImmutableListTypeAdapterFactory;
import ro.cosu.vampires.server.util.gson.ImmutableMapTypeAdapterFactory;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.writers.json.LocalDateTimeDeserializer;
import ro.cosu.vampires.server.writers.json.LocalDateTimeSerializer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class GsonTest {
    private static Gson getGson() throws FileNotFoundException {
        return new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .registerTypeAdapterFactory(new AutoValueAdapterFactory())
                .registerTypeAdapterFactory(new ImmutableMapTypeAdapterFactory())
                .registerTypeAdapterFactory(new ImmutableListTypeAdapterFactory())
                .create();
    }

    @Test
    public void testGsonAdapters() throws Exception {

        File tempFile = File.createTempFile("test", "gson");
        tempFile.deleteOnExit();
        Job expected = Job.empty();
        Gson gson = getGson();
        Writer fileWriter = Files.newWriter(tempFile, StandardCharsets.UTF_8);
        fileWriter.write(gson.toJson(expected));
        fileWriter.close();

        Reader reader = Files.newReader(tempFile, StandardCharsets.UTF_8);

        Job response = gson.fromJson(reader, Job.class);
        reader.close();

        assertThat(response, is(expected));

    }
}
