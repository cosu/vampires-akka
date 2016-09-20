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

package ro.cosu.vampires.server;

import com.google.common.collect.ImmutableList;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import ro.cosu.vampires.server.actors.BootstrapActor;
import ro.cosu.vampires.server.actors.Terminator;
import ro.cosu.vampires.server.actors.messages.configuration.CreateConfiguration;
import ro.cosu.vampires.server.actors.messages.execution.QueryExecution;
import ro.cosu.vampires.server.actors.messages.execution.ResponseExecution;
import ro.cosu.vampires.server.actors.messages.execution.StartExecution;
import ro.cosu.vampires.server.actors.messages.workload.CreateWorkload;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.ExecutionInfo;
import ro.cosu.vampires.server.values.jobs.ExecutionMode;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.jobs.WorkloadPayload;
import ro.cosu.vampires.server.values.resources.Configuration;
import ro.cosu.vampires.server.values.resources.ConfigurationPayload;
import ro.cosu.vampires.server.values.resources.ResourceDemand;
import ro.cosu.vampires.server.values.resources.ResourceDescription;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class BootstrapIT {
    @Test
    public void bootStrap() throws Exception {

        Config config = ConfigFactory.load("application-it.conf");

        final ActorSystem system = ActorSystem.create("ServerSystem", config);

        ActorRef terminator = system.actorOf(Terminator.props(), "terminator");
        ActorRef bootstrap = system.actorOf(BootstrapActor.props(terminator), "bootstrap");

        ImmutableList<ResourceDemand> resources = ImmutableList.of(ResourceDemand.builder().count(1)
                .resourceDescription(
                        ResourceDescription.builder()
                                .provider(Resource.ProviderType.LOCAL)
                                .type("local")
                                .cost(0)
                                .build()
                ).build());

        Configuration configuration = Configuration
                .fromPayload(
                        ConfigurationPayload.builder().description("integration testt")
                                .resources(resources)
                                .build()
                );

        Workload workload = Workload
                .fromPayload(
                        WorkloadPayload.builder().sequenceStart(0).sequenceStop(5).task("sleep 1")
                                .build());

        CreateWorkload createWorkload = CreateWorkload.create(workload, User.admin());
        CreateConfiguration createConfiguration = CreateConfiguration.create(configuration, User.admin());

        bootstrap.tell(createWorkload, ActorRef.noSender());
        bootstrap.tell(createConfiguration, ActorRef.noSender());


        Execution execution = Execution.builder().configuration(configuration)
                .type(ExecutionMode.FULL)
                .info(ExecutionInfo.empty())
                .workload(workload)
                .build();
        StartExecution startExecution = StartExecution.create(User.admin(), execution);
        bootstrap.tell(startExecution, ActorRef.noSender());

        int count = 0;
        int maxCount = 100;
        boolean running = true;

        Timeout timeout = new Timeout(Duration.create(100, "milliseconds"));

        while (running && count < maxCount) {

            Thread.sleep(2000);
            count++;

            Future<Object> ask = Patterns.ask(bootstrap,
                    QueryExecution.create(execution.id(), User.admin()), timeout);
            ResponseExecution responseExecution = (ResponseExecution) Await.result(ask, timeout.duration());
            execution = responseExecution.values().get(0);
            System.out.println(execution.info().status());
            running = execution.info().status().isActiveStatus();
        }
    }
}
