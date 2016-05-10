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
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.rest.controllers.ControllersModule;
import ro.cosu.vampires.server.rest.services.ConfigurationsService;
import ro.cosu.vampires.server.rest.services.ExecutionsService;
import ro.cosu.vampires.server.rest.services.WorkloadsService;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.Workload;
import spark.Spark;

public class BootstrapActor extends UntypedActor {

    private final ActorRef terminator;
    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef resourceManagerActor;
    private Map<String, Execution> executionMap = Maps.newHashMap();

    private ControllersModule controllersModule = new ControllersModule(getContext().system());
    private Injector injector;


    BootstrapActor(ActorRef terminator) {
        this.terminator = terminator;
    }

    public static Props props(ActorRef resourceManagerActor) {
        return Props.create(BootstrapActor.class, resourceManagerActor);
    }

    @Override
    public void preStart() {
        terminator.tell(new ResourceControl.Up(), getSelf());
        startWebserver();
        startFromConfig();
    }

    private void startWebserver() {
        Spark.port(settings.vampires.getInt("rest-port"));
        Spark.init();
        Spark.awaitInitialization();
        injector = Guice.createInjector(controllersModule);
    }

    private void startFromConfig() {
        if (settings.vampires.hasPath("start")) {

            ExecutionMode mode = settings.getMode();

            log.info("starting from config");
            // post to config service
            Workload workload = Workload.fromConfig(settings.vampires.getConfig("workload"));
            WorkloadsService workloadsService = injector.getInstance(WorkloadsService.class);
            workload = workloadsService.createWorkload(workload);

            // post to conf service
            Configuration configuration = Configuration.fromConfig(settings.vampires);
            ConfigurationsService configurationsService = injector.getInstance(ConfigurationsService.class);
            configuration = configurationsService.createConfiguration(configuration);

            ExecutionsService executionsService = injector.getInstance(ExecutionsService.class);
            Execution execution = Execution.builder().type(mode).configuration(configuration).workload(workload).build();
            executionsService.startExecution(execution);

        }
    }

    private void startExecution(Execution execution) {
        ActorRef workActor = getContext().actorOf(WorkActor.props(execution.workload(), execution.type()), "workActor");
        resourceManagerActor = getContext().actorOf(ResourceManagerActor.props(), "resourceManager");
        getContext().system().actorOf(DispatchActor.props(workActor), "server");
        executionMap.put(execution.id(), execution);
        resourceManagerActor.tell(execution, getSelf());
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
            //
        } else {
            unhandled(message);
        }

    }
}
