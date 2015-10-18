package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;


public class RegisterActor extends UntypedActor {
    final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    protected List<ActorRef> registered = new LinkedList<>();

    final ActorRef resourceManagerActor;

    public static Props props(){
        return Props.create(RegisterActor.class);
    }

    public RegisterActor() {
        resourceManagerActor = getContext().actorOf(ResourceManagerActor.props());
        getContext().watch(resourceManagerActor);
    }


    @Override
    public void preStart(){

        getContext().actorSelection("/user/terminator").tell(new Message.Up(), getSelf());

        settings.vampires.getConfigList("start").stream().forEach(config ->
                {
                    String type = config.getString("type");
                    int count = config.getInt("count");
                    String provider = config.getString("provider");
                    IntStream.rangeClosed(1, count).forEach(i ->
                            resourceManagerActor.tell(new Message.NewResource(Resource.Type.valueOf(provider
                                            .toUpperCase()), type),
                                    getSelf()));
                });
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.Up || (message instanceof ResourceInfo)) {
            registered.add(getSender());
            getContext().watch(getSender());
            log.info("up {}", getSender());
        } else if (message instanceof Terminated) {
            log.info("down {}",  getSender());
            boolean remove = registered.remove(getSender());
            getContext().unwatch(getSender());
            log.info("disconnected {} {}",remove , getSender());
            if (registered.isEmpty()){
                log.info("register actor stop");
                getContext().stop(getSelf());
            }
        } else if (message instanceof Message.Shutdown){
            log.info("shutdown");
            resourceManagerActor.forward(message, getContext());
            getContext().stop(getSelf());
        }

        else {
            log.info("unhandled {}", message);
            unhandled(message);
        }

    }
}
