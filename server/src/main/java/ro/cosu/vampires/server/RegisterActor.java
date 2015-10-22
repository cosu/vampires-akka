package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;

import java.util.stream.IntStream;


public class RegisterActor extends UntypedActor {
    final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    final ActorRef resourceManagerActor;

    public static Props props(ActorRef resourceManager){
        return Props.create(RegisterActor.class, resourceManager);
    }

    public RegisterActor(ActorRef resourceManager) {
        this.resourceManagerActor = resourceManager;

    }


    @Override
    public void preStart(){
        getSelf().tell(new ResourceControl.Up(), ActorRef.noSender());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ResourceControl.Up){
            startResources();
            log.info("starting resources");
        }

        if (message instanceof ResourceControl.Info) {
            log.info("got resource info {}", message);
        }
    }


    private void startResources() {
        settings.vampires.getConfigList("start").stream().forEach(config ->
                {
                    String type = config.getString("type");
                    int count = config.getInt("count");
                    String provider = config.getString("provider");
                    log.info("starting {} x  {} from provider {}", count, type, provider);

                    IntStream.rangeClosed(1, count).forEach(i ->
                            resourceManagerActor.tell(new ResourceControl.Start(Resource.Type.valueOf(provider
                                            .toUpperCase()), type), getSelf()));
                });
    }


}
