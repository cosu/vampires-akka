package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.Test;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.Metrics;
import ro.cosu.vampires.server.workload.Result;

public class WorkActorTest extends AbstractActorTest{
    @Test
    public void testWork(){
        new JavaTestKit(system) {
            {
                // create a test probe
                final JavaTestKit workProbe = new JavaTestKit(system);

                // create a work actor
                final Props props = WorkActor.props();
                final ActorRef forwarder = system.actorOf(props, "workactor");

                Job job = Job.builder()
                        .computation(Computation.builder().command("1").build())
                        .hostMetrics(Metrics.empty())
                        .result(Result.empty())
                        .build();

                forwarder.tell(job, workProbe.getRef());

                workProbe.expectMsgClass(Job.class);

            }
        };
    }
}
