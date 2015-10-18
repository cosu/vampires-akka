package ro.cosu.vampires.server.util;

import com.google.gson.*;
import ro.cosu.vampires.server.workload.Workload;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JsonResultsWriter implements ResultsWriter{
    @Override
    public void writeResults(List<Workload> results) {
        //write results to disk
        try {

            LocalDateTime date = LocalDateTime.now();

            Writer writer = new FileWriter(Paths.get(System.getProperty("user.home"), "results-" + date.format
                    (DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".json").toFile());

            Gson gson = new GsonBuilder().setPrettyPrinting()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                    .create();


            gson.toJson(results, writer);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();  //Auto-generated TODO
        }
    }


    public class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {

            return new JsonPrimitive(localDateTime.toString());
        }
    }
}
