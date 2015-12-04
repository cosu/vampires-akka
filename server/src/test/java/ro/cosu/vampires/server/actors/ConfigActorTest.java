package ro.cosu.vampires.server.actors;

import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.google.common.collect.Maps;
import org.junit.Test;
import ro.cosu.vampires.server.workload.ClientConfig;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Metrics;
import scala.concurrent.duration.Duration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConfigActorTest extends AbstractActorTest{

    @Test
    public void testGetConfig() throws Exception {
        final JavaTestKit remoteProbe = new JavaTestKit(system);

        TestActorRef<ConfigActor> config = TestActorRef.create(system, ConfigActor.props(), "config");
        Map<String, Integer> executors =Maps.newHashMap();
        executors.put("FORK", 1 );
        executors.put("DOCKER", 2);
        final ClientInfo clientInfo = ClientInfo.builder().executors(executors).metrics(Metrics.empty()).build();
        config.tell(clientInfo,remoteProbe.getRef() );

        remoteProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ClientConfig.class);





    }
}
