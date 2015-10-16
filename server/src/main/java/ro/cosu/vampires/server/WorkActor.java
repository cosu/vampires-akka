package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorkActor extends UntypedActor{


    final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());

    private final ConcurrentLinkedQueue<String> workQueue  = new ConcurrentLinkedQueue<>();

    ActorRef resultActor;

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

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


        if (message instanceof Message.Request) {
            log.info("Work request from " + getSender().toString());
            Object work = Optional.ofNullable(workQueue.poll())
                    .map(workItem -> (Object) new Message.Computation(workItem))
                    .orElse(PoisonPill.getInstance());

            getSender().tell(work, getSelf());
        } else if (message instanceof Message.Result) {
            resultActor.forward(message, getContext());
        } else {
            log.warning("unhandled message from {}", getSender() );
            unhandled(message);
        }





    }

}
