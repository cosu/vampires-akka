package ro.cosu.vampires.server.actors;


import com.google.common.collect.Sets;

import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import ro.cosu.vampires.server.actors.resource.ResourceControl;
import ro.cosu.vampires.server.actors.resource.ResourceManagerActor;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.Job;

public class ExecutionActor extends UntypedActor {

    Set<ActorRef> watchees = Sets.newLinkedHashSet();
    private ActorRef resourceManagerActor;
    private ActorRef resultActor;

    public ExecutionActor(Execution execution) {
        startExecution(execution);
    }

    public static Props props(Execution execution) {
        return Props.create(ExecutionActor.class, execution);
    }

    private void startExecution(Execution execution) {

        resourceManagerActor = getContext().actorOf(ResourceManagerActor.props(), "resourceActor");
        resourceManagerActor.tell(execution, getSelf());
        resultActor = getContext().actorOf(ResultActor.props(execution), "resultActor");
        getContext().watch(resourceManagerActor);
        getContext().watch(resultActor);

        watchees.add(resourceManagerActor);
        watchees.add(resultActor);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        // forward traffic to all actors
        if (message instanceof ClientInfo) {
            watchees.stream().forEach(actorRef -> actorRef.tell(message, getSender()));
        } else if (message instanceof Job) {
            resultActor.tell(message, getSender());
        } else if (message instanceof ResourceControl.Shutdown) {
            resourceManagerActor.tell(ResourceControl.Shutdown.create(), getSelf());
            resultActor.tell(ResourceControl.Shutdown.create(), getSelf());
        }
        if (message instanceof Terminated) {
            if (getSender().equals(resultActor)) {
                getContext().stop(getSelf());
            }
        } else {
            unhandled(message);
        }
    }
}
