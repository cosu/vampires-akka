package ro.cosu.vampires.server.actors;

import com.google.common.collect.Maps;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import ro.cosu.vampires.server.workload.ClientConfig;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Metrics;
import scala.concurrent.duration.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ConfigActorTest extends AbstractActorTest {

    @Test
    public void testGetConfig() throws Exception {
        TestProbe testProbe = new TestProbe(system);

        TestActorRef<ConfigActor> config = TestActorRef.create(system, ConfigActor.props());

        Map<String, Integer> executors = Maps.newHashMap();
        executors.put("FORK", 1);
        executors.put("DOCKER", 2);
        final ClientInfo clientInfo = ClientInfo.builder()
                .executors(executors)
                .metrics(Metrics.empty())
                .id("foo")
                .build();
        config.tell(clientInfo, testProbe.ref());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ClientConfig.class);
        ClientConfig clientConfig = (ClientConfig) testProbe.lastMessage().msg();
        assertThat(clientConfig.executor(), is("DOCKER"));
        assertThat(clientConfig.numberOfExecutors(), is(2));
        assertThat(clientConfig.cpuSetSize(), is(1));


    }

    @Test
    public void testEmptyConfig() throws Exception {
        final TestProbe testProbe = new TestProbe(system);

        TestActorRef<ConfigActor> config = TestActorRef.create(system, ConfigActor.props());
        Map<String, Integer> executors = Maps.newHashMap();

        final ClientInfo clientInfo = ClientInfo.builder()
                .executors(executors)
                .metrics(Metrics.empty())
                .id("foo")
                .build();
        config.tell(clientInfo, testProbe.ref());

        testProbe.expectMsgClass(Duration.create(1, TimeUnit.SECONDS), ClientConfig.class);
        ClientConfig clientConfig = (ClientConfig) testProbe.lastMessage().msg();
        assertThat(clientConfig, is(ClientConfig.empty()));
    }


}
