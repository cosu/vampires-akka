package ro.cosu.vampires.server.rest.services;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;

import ro.cosu.vampires.server.actors.AbstractActorTest;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionPayload;

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

    @Test
    @Ignore
    public void create() throws Exception {
        ExecutionPayload payload = ExecutionPayload.builder().configuration("foo")
                .type(Resource.ProviderType.MOCK.name()).workload("baz").build();
        Execution execution = executionsService.create(payload);
        assertThat(execution.status(), is("created"));
    }

    @Test
    @Ignore
    public void getExecutions() throws Exception {
        create();
        assertThat(executionsService.getExecutions().size(), is(1));
    }

    @Test
    @Ignore
    public void getExecution() throws Exception {
        create();
        Collection<Execution> executions = executionsService.getExecutions();
        Execution execution = executions.iterator().next();

        Optional<Execution> executionOptional = executionsService.getExecution(execution.id());

        assertThat(executionOptional.isPresent(), is(true));

        assertThat(executionOptional.get().id(), is(execution.id()));

    }


}