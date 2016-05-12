package ro.cosu.vampires.server.rest.services.di;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import com.typesafe.config.ConfigFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.ExecutionPayload;
import ro.cosu.vampires.server.workload.ResourceDemand;
import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EServiceTest extends AbstractDiTest<Execution, ExecutionPayload> {

    static ActorSystem system;

    public static ActorSystem getActorSystem() {
        return system;
    }

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("test", ConfigFactory.load("application-dev.conf"));
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Override
    protected AbstractModule getModule() {
        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(EService.getTypeTokenService()).to(new TypeLiteral<EService>() {
                });
            }

            @Provides
            private ActorRef provideActorSystem() {
                final JavaTestKit probe = new JavaTestKit(system);
                return probe.getRef();
            }

            @Provides
            private WService getWS() {
                String workloadConfig = "{\n" +
                        "    format = %08d.tif\n" +
                        "    sequenceStart = 0\n" +
                        "    sequenceStop = 10\n" +
                        "    task = \"echo\"\n" +
                        "    url = \"\"\n" +
                        "  }";

                WorkloadPayload workloadPayload = WorkloadPayload.fromConfig(ConfigFactory.parseString(workloadConfig));

                WService mock = mock(WService.class);
                when(mock.get(anyString())).thenReturn(Optional.of(Workload.fromPayload(workloadPayload)));
                return mock;
            }

            @Provides
            private CService getCS() {
                ImmutableList<ResourceDemand> resourceDemands = ImmutableList.of(ResourceDemand.builder().count(1)
                        .provider(Resource.ProviderType.MOCK).type("bar").build());
                ConfigurationPayload payload = ConfigurationPayload.create("foo", resourceDemands);
                Configuration configuration = Configuration.fromPayload(payload);
                CService mock = mock(CService.class);
                when(mock.get(anyString())).thenReturn(Optional.of(configuration));
                return mock;
            }
        };
        return module;
    }

    @Override
    protected TypeLiteral<Service<Execution, ExecutionPayload>> getTypeTokenService() {
        return EService.getTypeTokenService();
    }

    @Override
    protected ExecutionPayload getPayload() {
        return ExecutionPayload.builder().configuration("foo").workload("bar").type(ExecutionMode.FULL).build();
    }

    @Override
    @Test
    @Ignore
    public void delete() throws Exception {

    }

    @Override
    @Test
    @Ignore
    public void update() throws Exception {

    }
}