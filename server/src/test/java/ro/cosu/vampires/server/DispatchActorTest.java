package ro.cosu.vampires.server;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.Ignore;
import org.junit.Test;
import ro.cosu.vampires.server.workload.Computation;
import ro.cosu.vampires.server.workload.Metrics;
import ro.cosu.vampires.server.workload.Result;
import ro.cosu.vampires.server.workload.Job;

import static org.junit.Assert.assertEquals;

public class DispatchActorTest extends AbstractActorTest{


    @Test
    @Ignore
    public void testDispatch(){
        new JavaTestKit(system) {
            {
                // create a test probe
                final JavaTestKit workProbe = new JavaTestKit(system);

                // create a forwarder, injecting the probe’s testActor
                final Props props = DispatchActor.props(workProbe.getRef() );
                final ActorRef forwarder = system.actorOf(props, "dispatch");

                Job job = Job.builder()
                        .computation(Computation.builder().command("1").build())
                        .metrics(Metrics.empty())
                        .result(Result.empty())
                        .build();

                // verify correct forwarding

                forwarder.tell(job, getRef());

                assertEquals(getRef(), workProbe.getLastSender());
            }
        };
    }

}
