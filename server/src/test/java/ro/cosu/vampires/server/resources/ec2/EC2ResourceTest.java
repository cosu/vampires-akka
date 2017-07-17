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

package ro.cosu.vampires.server.resources.ec2;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EC2ResourceTest {
    private static final String INSTANCE_TYPE = "eu-west-1-t2-micro";
    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                MapBinder<Resource.ProviderType, ResourceProvider> mapbinder
                        = MapBinder.newMapBinder(binder(), Resource.ProviderType.class, ResourceProvider.class);

                mapbinder.addBinding(Resource.ProviderType.EC2).to(EC2ResourceProvider.class).asEagerSingleton();
            }

            @Provides
            @Named("Config")
            private Config provideConfig() {
                return ConfigFactory.load("application-dev.conf").getConfig("vampires");
            }

            @Provides
            private AmazonEC2 provideAmazonEc2(@Named("Config") Config config) {
                AmazonEC2Client ec2Client = mock(AmazonEC2Client.class);
                RunInstancesResult runInstancesResult = mock(RunInstancesResult.class, RETURNS_DEEP_STUBS);
                DescribeInstancesResult describeInstancesResult = mock(DescribeInstancesResult.class, RETURNS_DEEP_STUBS);
                TerminateInstancesResult terminateInstancesResult = mock(TerminateInstancesResult.class, RETURNS_DEEP_STUBS);

                when(runInstancesResult.getReservation().getInstances().get(0).getInstanceId()).thenReturn("foo");
                when(ec2Client.runInstances(any())).thenReturn(runInstancesResult);
                when(ec2Client.describeInstances(any())).thenReturn(describeInstancesResult);
                when(describeInstancesResult.getReservations().get(0).getInstances().get(0).getPublicDnsName())
                        .thenReturn("").thenReturn("foo");
                when(ec2Client.terminateInstances(any())).thenReturn(terminateInstancesResult);
                return ec2Client;
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

        ResourceProvider ec2Provider = rm.getProviders().get(Resource.ProviderType.EC2);
        Resource.Parameters parameters = ec2Provider.getParameters(instanceType);

        return ec2Provider.create(parameters).get();
    }

    @Test(expected = ConfigException.class)
    public void testInvalidInstanceType() throws Exception {
        getResource("foo");
    }


    @Test(expected = ConfigException.class)
    public void testInvalidRegion() throws Exception {
        getResource("eu.t2.micro");
    }

    @Test
    public void testEC2ClientFail() throws Exception {
        AmazonEC2 foo = EC2ResourceModule.getAmazonEC2Client("foo");
        assertThat(foo, is(nullValue()));
    }

    @Test
    public void testEC2ClientFailInvalidFormat() throws Exception {
        Path path = Files.createTempFile("aws", ".txt");
        File file = path.toFile();
        file.deleteOnExit();
        Files.write(path, "foo\nbar".getBytes(StandardCharsets.UTF_8));
        AmazonEC2 foo = EC2ResourceModule.getAmazonEC2Client(file.getAbsolutePath());
        assertThat(foo, is(nullValue()));
    }

    @Test
    public void testEC2Client() throws Exception {
        Path path = Files.createTempFile("aws", ".txt");
        File file = path.toFile();
        file.deleteOnExit();
        Files.write(path, "accessKey=foo\nsecretKey=bar".getBytes(StandardCharsets.UTF_8));
        AmazonEC2 foo = EC2ResourceModule.getAmazonEC2Client(file.getAbsolutePath());
        assertThat(foo, not(nullValue()));
    }

}