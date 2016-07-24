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

package ro.cosu.vampires.client;

import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Option;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.cosu.vampires.client.actors.ClientActor;
import ro.cosu.vampires.client.actors.MonitoringActor;
import ro.cosu.vampires.server.values.ClientConfig;
import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Computation;
import ro.cosu.vampires.server.values.jobs.Job;
import scala.concurrent.duration.Duration;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ClientActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setUp() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void tearDown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testClientActor() throws Exception {

        TestActorRef.create(system, MonitoringActor
                .props(TestUtil.getMetricRegistryMock()), "monitor");

        TestActorRef<ClientActor> client = TestActorRef.create(system, ClientActor.props("test", "client1"), "client1");

        final JavaTestKit remoteProbe = new JavaTestKit(system);

        scala.Option<ActorRef> actorRefOption = Option.Some.option(remoteProbe.getRef()).asScala();
        client.tell(new ActorIdentity(null, actorRefOption), ActorRef.noSender());

        ClientInfo clientInfo = (ClientInfo) remoteProbe.receiveOne(Duration.create("500 milliseconds"));
        assertThat(clientInfo.executors().size(), not(0));
        ClientConfig clientConfig = ClientConfig.withDefaults().numberOfExecutors(1).build();
        client.tell(clientConfig, remoteProbe.getRef());
        Job job = (Job) remoteProbe.receiveOne(Duration.create("500 milliseconds"));
        assertThat(job.computation(), is(Computation.empty()));

    }
}
