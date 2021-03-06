/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.resources;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import com.typesafe.config.ConfigFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import ro.cosu.vampires.server.resources.das5.Das5ResourceParameters;
import ro.cosu.vampires.server.util.SshClient;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ResourceManagerIT {



    private Das5ResourceParameters getDasConfig() {
        return Das5ResourceParameters.builder()
                .command("vampires.sh")
                .user("cosmin")
                .address("localhost")
                .privateKey("~/.ssh/id_rsa")
                .port(2222)
                .build();
    }

    @Test
    @Ignore
    public void testCreateLocalResource() throws  Exception  {

        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.load().getConfig("vampires")));

        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider localProvider = rm.getProviders().get(Resource.ProviderType.LOCAL);
        Resource.Parameters parameters = localProvider.getParameters("local");
        Resource resource = localProvider.create(parameters).get();

        assertThat(resource.start().get(2, TimeUnit.SECONDS).status(), is(Resource.Status.RUNNING));
        assertThat(resource.stop().get(1, TimeUnit.SECONDS).status(), is(Resource.Status.STOPPED));

    }

    @Test
    @Ignore
    public void testCreateSSHResource() throws  Exception  {
        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.load().getConfig("vampires")));
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider sshProvider = rm.getProviders().get(Resource.ProviderType.SSH);
        Resource.Parameters parameters = sshProvider.getParameters("local");

        Resource resource = sshProvider.create(parameters).get();

        assertThat(resource.start().get(1, TimeUnit.SECONDS).status(), is(Resource.Status.RUNNING));
        assertThat(resource.stop().get(1, TimeUnit.SECONDS).status(), is(Resource.Status.STOPPED));

    }

    @Test
    @Ignore
    public void testCreateDAS5Resource() throws  Exception  {

        SshClient sshClientMock = mock(SshClient.class);

        Mockito.when(sshClientMock.runCommand(Mockito.anyString(),Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("10 10 10 10");

        class TestModule extends AbstractModule {

            @Override
            protected void configure() {
                bind(SshClient.class).toInstance(sshClientMock);
            }
        }

        Module override = Modules.override(new ResourceModule(ConfigFactory.empty())).with(new TestModule());
        Injector injector = Guice.createInjector(override );
        ResourceManager rm = injector.getInstance(ResourceManager.class);
        ResourceProvider localProvider = rm.getProviders().get(Resource.ProviderType.DAS5);
        Resource resource = localProvider.create(getDasConfig()).get();

        assertThat(resource.start().get(2, TimeUnit.SECONDS).status(), is(Resource.Status.RUNNING));
        Thread.sleep(2000);
        assertThat(resource.stop().get(2, TimeUnit.SECONDS).status(), is(Resource.Status.STOPPED));
    }

}
