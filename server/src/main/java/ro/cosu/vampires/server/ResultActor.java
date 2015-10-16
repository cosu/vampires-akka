package ro.cosu.vampires.server;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ResultActor extends UntypedActor{
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private List<Message.Result> results = new LinkedList<>();

    public static Props props(){
        return Props.create(ResultActor.class);
    }


    public  void preStart() {
        getContext().actorSelection("/user/terminator").tell(new Message.Up(), getSelf());
    }

    @Override
    public void postStop(){
        //write results to disk
        try {

            LocalDateTime date = LocalDateTime.now();

            Writer writer = new FileWriter(Paths.get(System.getProperty("user.home"), "results.json", date.toString()).toFile());

            Gson gson = new GsonBuilder().setPrettyPrinting()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                        .create();


            gson.toJson(results, writer);

        } catch (IOException e) {
            e.printStackTrace();  //Auto-generated TODO
        }


    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.Result) {
            results.add((Message.Result) message);
            log.debug("got result {}", message);
        }
    }

    public class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
            Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
            Date date = Date.from(instant);
            return new JsonPrimitive(date.getTime());
        }
    }
}
