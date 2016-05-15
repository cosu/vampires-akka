package ro.cosu.vampires.server.actors;


import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.rest.RestModule;
import ro.cosu.vampires.server.rest.services.ConfigurationsService;
import ro.cosu.vampires.server.rest.services.WorkloadsService;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.WorkloadPayload;
import spark.Spark;

public class BootstrapActor extends UntypedActor {

    private final ActorRef terminator;
    private final SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private Map<String, ActorRef> executionMap = Maps.newHashMap();

    private RestModule restModule = new RestModule(getSelf(), settings.getProviders());
    private Injector injector;


    BootstrapActor(ActorRef terminator) {
        this.terminator = terminator;
    }

    public static Props props(ActorRef terminator) {
        return Props.create(BootstrapActor.class, terminator);
    }

    @Override
    public void preStart() {
        terminator.tell(new ResourceControl.Up(), getSelf());
        startWebserver();
        loadFromConfig();
    }

    private void startWebserver() {
        Spark.port(settings.vampires.getInt("rest-port"));
        Spark.init();
        injector = Guice.createInjector(restModule);

    }

    private void loadFromConfig() {
        if (settings.vampires.hasPath("workloads")) {
            WorkloadsService workloadsService = injector.getInstance(WorkloadsService.class);
            settings.vampires.getConfigList("workloads").stream()
                    .map(WorkloadPayload::fromConfig)
                    .forEach(workloadsService::create);
        }

        if (settings.vampires.hasPath("configurations")) {
            ConfigurationsService configurationsService = injector.getInstance(ConfigurationsService.class);
            settings.vampires.getConfigList("configurations").stream()
                    .map(ConfigurationPayload::fromConfig)
                    .forEach(configurationsService::create);
        }
    }

    private void startExecution(Execution execution) {
        ActorRef executionActor = getContext().system().actorOf(ExecutionActor.props(execution), execution.id());
        executionMap.put(execution.id(), executionActor);
        executionActor.tell(execution, getSelf());
    }

    @Override
    public void postStop() {
        Spark.stop();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Execution) {
            Execution execution = (Execution) message;
            startExecution(execution);
        } else if (message instanceof ResourceInfo) {

        } else {
            unhandled(message);
        }

    }
}
