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

package ro.cosu.vampires.server.rest.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import com.typesafe.config.ConfigFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import akka.testkit.TestActor;
import ro.cosu.vampires.server.actors.messages.execution.DeleteExecution;
import ro.cosu.vampires.server.actors.messages.execution.QueryExecution;
import ro.cosu.vampires.server.actors.messages.execution.ResponseExecution;
import ro.cosu.vampires.server.actors.messages.execution.StartExecution;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.ExecutionInfo;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.jobs.WorkloadPayload;
import ro.cosu.vampires.server.values.resources.Configuration;
import ro.cosu.vampires.server.values.resources.ConfigurationPayload;
import ro.cosu.vampires.server.values.resources.ResourceDemand;
import ro.cosu.vampires.server.values.resources.ResourceDescription;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ServicesTestModule extends AbstractModule {

    private final ActorRef actorRef;
    private Configuration configuration = Configuration.fromPayload(getConfigurationPayload());
    private Workload workload = Workload.fromPayload(getWorkloadPayload());

    public ServicesTestModule(ActorSystem actorSystem) {
        TestKit probe = new TestKit(actorSystem);
        probe.setAutoPilot(new TestActor.AutoPilot() {
            Map<String, Execution> executionMap = Maps.newHashMap();

            @Override
            public TestActor.AutoPilot run(ActorRef sender, Object msg) {
                if (msg instanceof StartExecution) {
                    StartExecution startExecution = (StartExecution) msg;
                    executionMap.put(startExecution.execution().id(), startExecution.execution());
                } else if (msg instanceof QueryExecution) {
                    QueryExecution info = (QueryExecution) msg;

                    List<Execution> executionList;
                    if (info.equals(QueryExecution.all(info.user()))) {
                        executionList = new ArrayList<>(executionMap.values());
                    } else {
                        executionList = Optional.ofNullable(executionMap.get(info.resourceId()))
                                .map(Lists::newArrayList)
                                .orElse(Lists.newArrayList());
                    }
                    sender.tell(ResponseExecution.create(executionList), actorRef);

                } else if (msg instanceof DeleteExecution) {
                    Optional<Execution> executionOptional = Optional.ofNullable(executionMap.get(((DeleteExecution) msg).resourceId()));

                    ResponseExecution responseExecution = executionOptional.map(execution -> {
                        ExecutionInfo executionInfo = execution.info().updateStatus(ExecutionInfo.Status.CANCELED);
                        execution = execution.withInfo(executionInfo);
                        executionMap.put(execution.id(), execution.withInfo(executionInfo));
                        return ResponseExecution.create(ImmutableList.of(execution));
                    }).orElse(ResponseExecution.create(ImmutableList.of()));

                    sender.tell(responseExecution, actorRef);

                } else {
                    fail();
                }

                return keepRunning();
            }
        });
        this.actorRef = probe.testActor();
    }

    private WorkloadPayload getWorkloadPayload() {

        String workloadConfig = "{\n" +
                "    format = %08d.tif\n" +
                "    sequenceStart = 0\n" +
                "    sequenceStop = 10\n" +
                "    task = \"echo\"\n" +
                "    url = \"\"\n" +
                "  }";

        return WorkloadPayload.fromConfig(ConfigFactory.parseString(workloadConfig));
    }

    private ConfigurationPayload getConfigurationPayload() {
        ImmutableList<ResourceDemand> resourceDemands = ImmutableList.of(ResourceDemand.builder().count(1)
                .resourceDescription(
                        ResourceDescription.builder().provider(Resource.ProviderType.MOCK).resourceType("bar").cost(0).build()
                )
                .build());
        return ConfigurationPayload.builder().description("foo").resources(resourceDemands).build();
    }

    @Override
    protected void configure() {
        bind(ExecutionsService.getTypeTokenService()).to(new TypeLiteral<ExecutionsService>() {
        });
    }

    @Provides
    private ActorRef getActorRef() {
        return actorRef;
    }


    @Provides
    public Configuration getConfiguration() {
        return configuration;
    }

    @Provides
    public Workload getWorkload() {
        return workload;
    }

    @Provides
    private WorkloadsService getWS() {
        WorkloadsService mock = mock(WorkloadsService.class);
        when(mock.get(any(), eq(User.admin()))).thenReturn(Optional.of(workload));
        return mock;
    }

    @Provides
    private ConfigurationsService getCS() {
        ConfigurationsService mock = mock(ConfigurationsService.class);
        when(mock.get(any(), eq(User.admin()))).thenReturn(Optional.of(configuration));
        return mock;
    }
}
