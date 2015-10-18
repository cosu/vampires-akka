package ro.cosu.vampires.server.writers.json;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.writers.ResultsWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class JsonResultsWriter implements ResultsWriter {
    static final Logger LOG = LoggerFactory.getLogger(JsonResultsWriter.class);

    List<Workload> results = new LinkedList<>();

    @Override
    public void writeResult(Workload result) {
        results.add(result);
    }
    public void close() {
        //write results to disk
        try {
            LocalDateTime date = LocalDateTime.now();
            File resultsFile = Paths.get(System.getProperty("user.home"), "results-" + date.format
                    (DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".json").toFile();


            Writer writer = new FileWriter(resultsFile);

            Gson gson = new GsonBuilder().setPrettyPrinting()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                    .create();


            gson.toJson(results, writer);
            writer.close();
            LOG.info("wrote results to {}", resultsFile.getAbsolutePath());

        } catch (IOException e) {
           LOG.error("Error writing results to file", e);
        }
    }


    public class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {

            return new JsonPrimitive(localDateTime.toString());
        }
    }
}
