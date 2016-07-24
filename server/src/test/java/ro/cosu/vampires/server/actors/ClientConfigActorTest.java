/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.actors;

import com.google.common.collect.Maps;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import ro.cosu.vampires.server.values.ClientConfig;
import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;
import scala.concurrent.duration.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ClientConfigActorTest extends AbstractActorTest {

    @Test
    public void testGetConfig() throws Exception {
        TestProbe testProbe = new TestProbe(system);

        TestActorRef<ClientConfigActor> config = TestActorRef.create(system, ClientConfigActor.props());

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

        TestActorRef<ClientConfigActor> config = TestActorRef.create(system, ClientConfigActor.props());
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
