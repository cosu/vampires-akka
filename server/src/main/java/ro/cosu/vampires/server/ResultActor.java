package ro.cosu.vampires.server;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
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

            Writer writer = new FileWriter(Paths.get(System.getProperty("user.home"), "results.json").toFile());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
}
