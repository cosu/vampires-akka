package ro.cosu.vampires.server;

import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.resources.Settings;
import ro.cosu.vampires.resources.SettingsImpl;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorkActor extends UntypedActor{


    final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    private final ConcurrentLinkedQueue<String> workQueue  = new ConcurrentLinkedQueue<>();
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props(){
        return Props.create(WorkActor.class);
    }

    public WorkActor() {

        log.info("created work actor");
        initq();
    }


    @Override
    public  void preStart() {
        getContext().actorSelection("/user/terminator").tell(new Message.Up(), getSelf());
    }

    private void initq (){
        log.info("adding work init");
        settings.getWorkload().stream().forEach(workQueue::add);


//        getContext().system().scheduler().schedule(Duration.create(5, SECONDS),
//                Duration.create(5, SECONDS), () -> {
//                    int diff = 10 - workQueue.size();
//                    if (diff > 0) {
//                        log.info("adding work");
//                        IntStream.range(10-diff, 11).forEach(workQueue::add);
//                    }
//                }, getContext().system().dispatcher());
    }

    @Override
    public void onReceive(Object msg) throws Exception {

        log.info("Work request from " + getSender().toString());

            Object work = Optional.ofNullable(workQueue.poll())
                .map(workItem -> (Object) new Message.Computation(workItem))
                    .orElse(PoisonPill.getInstance());

            getSender().tell(work, getSelf());

        if (workQueue.isEmpty()) {
            getContext().actorSelection("/user/terminator").tell(new Message.Shutdown(), getSelf());
        }
    }

}
