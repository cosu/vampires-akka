package ro.cosu.vampires.server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Job;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorkActor extends UntypedActor{


    final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    private final ConcurrentLinkedQueue<String> workQueue  = new ConcurrentLinkedQueue<>();



    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef resultActor;

    public static Props props(){
        return Props.create(WorkActor.class);
    }


    @Override
    public  void preStart() {
        initq();
        resultActor = getContext().actorOf(ResultActor.props(workQueue.size()), "resultActor");

    }

    private void initq (){
        log.info("adding work init");
        settings.getWorkload().stream().forEach(workQueue::add);
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Job) {
            resultActor.forward(message, getContext());
            log.debug("Work result from {}", getSender().toString());
            Object work = this.getNewWorkload(Optional.ofNullable(workQueue.poll()));
            getSender().tell(work, getSelf());

        } else if (message instanceof ResourceControl.Shutdown){
            log.info("shutting down");
            resultActor.forward(message, getContext());
            getContext().stop(getSelf());
        } else {
            log.warning("unhandled message from {}", getSender() );
            unhandled(message);
        }


    }

    private Object getNewWorkload(Optional<String> work) {

        if (work.isPresent()) {
            Computation computation = Computation.builder().command(work.get()).build();
            log.debug("computation {}", computation);
            return Job.empty().withComputation(computation);

        }
        else {
            log.debug("Empty {}", getSender());

            return Job.empty().withComputation(Computation.builder().command("sleep 9999").build());

        }

    }

}
