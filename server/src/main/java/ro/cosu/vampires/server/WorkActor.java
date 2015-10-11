package ro.cosu.vampires.server;

import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

public class WorkActor extends UntypedActor{


    private final ConcurrentLinkedQueue<Integer> workQueue  = new ConcurrentLinkedQueue<>();
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
        IntStream.range(1, 100).forEach(workQueue::add);

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
                .map(num -> (Object) new Message.Computation(String.valueOf(num)))
                    .orElse(PoisonPill.getInstance());

            getSender().tell(work, getSelf());

        if (workQueue.isEmpty()) {
            getContext().actorSelection("/user/terminator").tell(new Message.Shutdown(), getSelf());
        }
    }

}
