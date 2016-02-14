package ro.cosu.vampires.server.resources.ec2;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;


public class EC2ResourceTest {
    private static final String INSTANCE_TYPE = "eu-west-1.t2.micro";
    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                MapBinder<Resource.Type, ResourceProvider> mapbinder
                        = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);

                mapbinder.addBinding(Resource.Type.EC2).to(EC2ResourceProvider.class).asEagerSingleton();
            }

            @Provides
            @Named("Config")
            private Config provideConfig() {
                return ConfigFactory.parseString(
                        "resources.ec2 { " +
                            "command=bar\n" +
                            "imageId=baz\n" +
                            "keyName=foo\n" +
                            "securityGroup=foo\n" +
                            "eu-west-1.t2.medium {\n" +
                                "region=eu-east-1\n" +
                                "instanceType=t2.medium\n" +
                                "}" +
                        "}"
                );
            }

            @Provides
            private Optional<AmazonEC2Client> provideAmazonEc2(@Named("Config") Config config) {
                AmazonEC2Client ec2Client = Mockito.mock(AmazonEC2Client.class);
                RunInstancesResult runInstancesResult = Mockito.mock(RunInstancesResult.class, RETURNS_DEEP_STUBS);
                DescribeInstancesResult describeInstancesResult = mock(DescribeInstancesResult.class, RETURNS_DEEP_STUBS);
                TerminateInstancesResult terminateInstancesResult = mock(TerminateInstancesResult.class, RETURNS_DEEP_STUBS);

                when(runInstancesResult.getReservation().getInstances().get(0).getInstanceId()).thenReturn("foo");
                when(ec2Client.runInstances(anyObject())).thenReturn(runInstancesResult);
                when(ec2Client.describeInstances(anyObject())).thenReturn(describeInstancesResult);
                when(describeInstancesResult.getReservations().get(0).getInstances().get(0).getPublicDnsName())
                        .thenReturn("bar");
                when(ec2Client.terminateInstances(anyObject())).thenReturn(terminateInstancesResult);
                return Optional.of(ec2Client);
            }
        });
    }

    @Test
    public void createEC2Resource() throws Exception {

        Resource resource = getResource(INSTANCE_TYPE);
        assertThat(resource.status(), equalTo(Resource.Status.SLEEPING));
    }

    @Test
    public void testStartStopEC2Resource() throws Exception {
        Resource resource = getResource(INSTANCE_TYPE);
        assertThat(resource.start().get().status(), is(Resource.Status.RUNNING));
        assertThat(resource.stop().get().status(), is(Resource.Status.STOPPED));
    }

    private Resource getResource(String instanceType) {
        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider ec2Provider = rm.getProviders().get(Resource.Type.EC2);
        Resource.Parameters parameters = ec2Provider.getParameters(instanceType);

        return ec2Provider.create(parameters).get();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInstanceType() throws Exception {
        getResource("foo");
    }

    @Test
    public void testCustomInstanceType() throws Exception {
        AbstractResource resource = (EC2Resource) getResource("eu-west-1.t2.medium");
        EC2ResourceParameters parameters = (EC2ResourceParameters) resource.getParameters();
        assertThat(parameters.region(), is("eu-east-1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRegion() throws Exception {
        getResource("eu.t2.micro");
    }


    
}