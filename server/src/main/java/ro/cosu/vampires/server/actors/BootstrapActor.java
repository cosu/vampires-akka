package ro.cosu.vampires.server.actors;


import com.google.common.collect.Maps;

import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.Workload;

public class BootstrapActor extends UntypedActor {

    private final ActorRef terminator;
    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef resourceManagerActor;
    private Map<String, Execution> executionMap = Maps.newHashMap();

    BootstrapActor(ActorRef terminator) {
        this.terminator = terminator;
    }

    public static Props props(ActorRef resourceManagerActor) {
        return Props.create(BootstrapActor.class, resourceManagerActor);
    }

    @Override
    public void preStart() {
        terminator.tell(new ResourceControl.Up(), getSelf());
        startConfiguration();
    }

    private void startConfiguration() {
        log.info("{}", settings.vampires);
        if (settings.vampires.hasPath("start")) {
            log.info("foo");
            Configuration configuration = Configuration.fromConfig(settings.vampires);

            ExecutionMode mode = settings.getMode();

            Workload workload = Workload.fromConfig(settings.vampires.getConfig("workload"));

            Execution execution = Execution.builder()
                    .configuration(configuration)
                    .workload(workload)
                    .type(mode).build();

            ActorRef workActor = getContext().actorOf(WorkActor.props(workload, mode), "workActor");
            resourceManagerActor = getContext().actorOf(ResourceManagerActor.props(), "resourceManager");
            getContext().system().actorOf(DispatchActor.props(workActor), "server");

            getSelf().tell(execution, getSelf());
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Execution) {
            Execution execution = (Execution) message;
            executionMap.put(execution.id(), execution);
            resourceManagerActor.tell(execution, getSelf());
            log.info("sending {} to {}", execution, resourceManagerActor);
        } else if (message instanceof ResourceInfo) {
            //
        } else {
            unhandled(message);
        }

    }
}
