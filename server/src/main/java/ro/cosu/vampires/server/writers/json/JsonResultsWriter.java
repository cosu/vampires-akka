package ro.cosu.vampires.server.writers.json;

import autovalue.shaded.com.google.common.common.collect.Maps;
import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.WebsocketHandler;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.writers.ResultsWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonResultsWriter implements ResultsWriter {
    private static final Logger LOG = LoggerFactory.getLogger(JsonResultsWriter.class);
    private final Config config;

    private List<Job> results = new LinkedList<>();
    private List<ClientInfo> clients = new LinkedList<>();


    public JsonResultsWriter(Config config) {
        this.config = config;

        Preconditions.checkArgument(config.hasPath("writers.json.dir"), "missing  config key writers.json.dir");
        Preconditions.checkArgument(Paths.get(config.getString("writers.json.dir")).toFile().canWrite(), "output dir " +
                "does not exist");

    }

    private Path getPath() {
        LocalDateTime date = LocalDateTime.now();
        return Paths.get(config.getString("writers.json.dir"), "results-" + date.format
                (DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".json");

    }

    @Override
    public void addResult(Job result) {
//        Gson gson = new GsonBuilder().setPrettyPrinting()
//                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
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
        try {
            File resultsFile = getPath().toFile();
            Writer writer = new FileWriter(resultsFile);

            Gson gson = new GsonBuilder().setPrettyPrinting()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                    .create();

            Map<String, Object> res = Maps.newHashMap();
            res.put("results", results);
            res.put("clients", clients);
            gson.toJson(res, writer);
            writer.close();
            LOG.info("wrote results to {}", resultsFile.getAbsolutePath());

        } catch (IOException e) {
            LOG.error("Error writing results to file", e);
        }
    }


    public class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext
                jsonSerializationContext) {

            return new JsonPrimitive(localDateTime.toString());
        }
    }
}
