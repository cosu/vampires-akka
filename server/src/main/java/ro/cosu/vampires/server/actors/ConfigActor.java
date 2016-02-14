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
            log.info("config for client {}:{} {}", clientInfo.metrics().metadata().get("host-hostname"),
                    clientInfo.id(),configFor);
            getSender().tell(configFor, getSelf());
            getContext().actorSelection("/user/resourceManager").forward(clientInfo, getContext());
            getContext().actorSelection("/user/workActor/resultActor").forward(clientInfo, getContext());
        } else {
            unhandled(message);
        }
    }

    private ClientConfig getConfigFor(ClientInfo clientInfo) {

        //take the first executor defined in the config
        final Optional<String> firstAvailableExecutor = settings
                .getExecutors().stream()
                .filter(ex -> clientInfo.executors().containsKey(ex))
                .findFirst();


        if (!firstAvailableExecutor.isPresent()) {
            log.error("client has reported unsupported executors: {} ", clientInfo.executors());
            return ClientConfig.empty();
        }

        final String executor = firstAvailableExecutor.get();
        int executorCpuCount = clientInfo.executors().get(executor);

        int cpuSetSize = Math.min(executorCpuCount, settings.getCpuSetSize());
        //this happens if the config says use 2 cpus but the machine has only 1.

        if (executorCpuCount < settings.getCpuSetSize()) {
            log.warning("Client reported less cpus ({}) than CPU_SET_SIZE ({})", executorCpuCount,
                    settings.getCpuSetSize());
        }
        int numberOfExecutors = executorCpuCount/ cpuSetSize;

        return ClientConfig.builder()
                .cpuSetSize(cpuSetSize)
                .executor(executor)
                .numberOfExecutors(numberOfExecutors)
                .build();
    }

    public static Props props() {
        return Props.create(ConfigActor.class);
    }
}
