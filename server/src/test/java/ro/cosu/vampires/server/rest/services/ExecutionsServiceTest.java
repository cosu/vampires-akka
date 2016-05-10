package ro.cosu.vampires.server.rest.services;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.typesafe.config.ConfigFactory;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;

import ro.cosu.vampires.server.actors.AbstractActorTest;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.ExecutionPayload;
import ro.cosu.vampires.server.workload.Workload;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class ExecutionsServiceTest extends AbstractActorTest {
    private static Injector injector;
    private ExecutionsService executionsService;
    private WorkloadsService workloadsService;
    private ConfigurationsService configurationsService;

    @BeforeClass
    public static void setUpClass() {
        ServicesModule controllersModule = new ServicesModule(getActorSystem());
        injector = Guice.createInjector(controllersModule);

    }

    @AfterClass
    public static void tearDown() {
        injector = null;
    }


    @Before
    public void setUp() {
        executionsService = injector.getInstance(ExecutionsService.class);
        workloadsService = injector.getInstance(WorkloadsService.class);
        configurationsService = injector.getInstance(ConfigurationsService.class);

    }

    private Configuration createConfiguration() {
        String startConfig = "{" +
                "description = foo\n" +
                "start  = [\n" +
                "    {\n" +
                "      provider = local\n" +
                "      type = local\n" +
                "      count = 1\n" +
                "    }\n" +
                "  ]" +
                "}";

        return configurationsService
                .createConfiguration(Configuration.fromConfig(ConfigFactory.parseString(startConfig)));

    }

    private Workload createWorkload() {
        String workloadConfig = "{\n" +
                "    format = %08d.tif\n" +
                "    sequenceStart = 0\n" +
                "    sequenceStop = 10\n" +
                "    task = \"echo\"\n" +
                "    url = \"\"\n" +
                "  }";

        return workloadsService.createWorkload(Workload.fromConfig(ConfigFactory.parseString(workloadConfig)));
    }


    @Test
    public void create() throws Exception {
        Configuration configuration = createConfiguration();
        Workload workload = createWorkload();

        ExecutionPayload payload = ExecutionPayload.builder().configuration(configuration.id())
                .type(ExecutionMode.FULL.name()).workload(workload.id()).build();
        Execution execution = executionsService.create(payload);
        assertThat(execution.status(), is("created"));
    }

    @Test
    public void getExecutions() throws Exception {
        create();
        assertThat(executionsService.getExecutions().size(), is(1));
    }

    @Test
    public void getExecution() throws Exception {
        create();
        Collection<Execution> executions = executionsService.getExecutions();
        Execution execution = executions.iterator().next();

        Optional<Execution> executionOptional = executionsService.getExecution(execution.id());

        assertThat(executionOptional.isPresent(), is(true));

        assertThat(executionOptional.get().id(), is(execution.id()));
    }

}