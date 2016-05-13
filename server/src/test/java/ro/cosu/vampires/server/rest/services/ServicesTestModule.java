package ro.cosu.vampires.server.rest.services;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import com.typesafe.config.ConfigFactory;

import java.util.Optional;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.ResourceDemand;
import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ServicesTestModule extends AbstractModule {

    private final ActorRef actorRef;
    private Configuration configuration = Configuration.fromPayload(getConfigurationPayload());
    private Workload workload = Workload.fromPayload(getWorkloadPayload());

    public ServicesTestModule(ActorSystem actorSystem) {
        JavaTestKit probe = new JavaTestKit(actorSystem);
        this.actorRef = probe.getRef();
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
                .provider(Resource.ProviderType.MOCK).type("bar").build());
        return ConfigurationPayload.create("foo", resourceDemands);
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
        when(mock.get(anyString())).thenReturn(Optional.of(workload));
        return mock;
    }

    @Provides
    private ConfigurationsService getCS() {
        ConfigurationsService mock = mock(ConfigurationsService.class);
        when(mock.get(anyString())).thenReturn(Optional.of(configuration));
        return mock;
    }
}
