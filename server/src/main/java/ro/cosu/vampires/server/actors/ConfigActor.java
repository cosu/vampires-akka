package ro.cosu.vampires.server.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.ClientConfig;
import ro.cosu.vampires.server.workload.ClientInfo;

import java.util.Optional;

public class ConfigActor extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ClientInfo) {
            ClientInfo clientInfo = (ClientInfo) message;
            final ClientConfig configFor = getConfigFor(clientInfo);
            log.info("config for client {}", configFor);
            getSender().tell(configFor, getSelf());
        } else {
            unhandled(message);
        }
    }

    private ClientConfig getConfigFor(ClientInfo clientInfo) {

        //take the first executor defined in the config
        final Optional<String> firstAvailableExecutor = settings.getExecutors().stream().filter(ex -> clientInfo
                .executors().containsKey(ex)).findFirst();


        if (!firstAvailableExecutor.isPresent()) {
            log.error("client has reported unsupported executors: {} ", clientInfo.executors());
            throw new IllegalArgumentException("unsupported executors");
        }

        final String executor = firstAvailableExecutor.get();

        int numberOfExecutors = clientInfo.executors().get(executor) / settings.getCpuSetSize();

        return ClientConfig.builder()
                .cpuSetSize(settings.getCpuSetSize())
                .executor(executor)
                .numberOfExecutors(numberOfExecutors)
                .build();
    }

    public static Props props() {
        return Props.create(ConfigActor.class);
    }
}
