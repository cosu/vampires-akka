package ro.cosu.vampires.server.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import ro.cosu.vampires.server.util.gson.AutoValueAdapterFactory;
import ro.cosu.vampires.server.util.gson.ImmutableListTypeAdapterFactory;
import ro.cosu.vampires.server.util.gson.ImmutableMapTypeAdapterFactory;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.writers.json.LocalDateTimeDeserializer;
import ro.cosu.vampires.server.writers.json.LocalDateTimeSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class GsonTest {
    private static Gson getGson() throws FileNotFoundException {
        return new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .registerTypeAdapterFactory(new AutoValueAdapterFactory())
                .registerTypeAdapterFactory(new ImmutableMapTypeAdapterFactory())
                .registerTypeAdapter(ImmutableMap.class, ImmutableMapTypeAdapterFactory.newCreator())
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
