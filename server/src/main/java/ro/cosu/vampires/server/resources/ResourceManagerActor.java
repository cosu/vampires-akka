package ro.cosu.vampires.server.resources;

import akka.actor.Props;
import akka.actor.UntypedActor;
import com.typesafe.config.Config;

public class ResourceManagerActor extends UntypedActor {

    private Config config;

    public static Props props(Config config){
        return Props.create(ResourceManagerActor.class, config);
    }

    public ResourceManagerActor(Config config) {
        this.config = config;
    }

    @Override
    public void onReceive(Object message) throws Exception {

    }
}
