package ro.cosu.vampires.client;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import ro.cosu.vampires.client.executors.DockerExecutor;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.executors.ExecutorsManager;
import ro.cosu.vampires.client.executors.ExecutorsModule;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.Result;

public class ExecutorActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    public static Props props() {
        return Props.create(ExecutorActor.class);
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Job) {
            Job job = (Job) message;

            Injector injector = Guice.createInjector(new ExecutorsModule(ConfigFactory.load().getConfig("vampires")));

            ExecutorsManager em = injector.getInstance(ExecutorsManager.class);

            Executor executor;
            if (DockerExecutor.isAvailable()) {

                executor = em.getProvider(Executor.Type.DOCKER).get();
            }
            else {
                executor = em.getProvider(Executor.Type.COMMAND).get();
            }

            Result result = executor.execute(job.computation());

            getContext().actorSelection("/user/monitor").tell(job.withResult(result), getSender());

        }

        getContext().stop(getSelf());

    }


}
