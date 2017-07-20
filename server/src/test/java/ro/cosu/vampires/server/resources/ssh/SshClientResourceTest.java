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

package ro.cosu.vampires.server.resources.ssh;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;

import com.jcraft.jsch.JSchException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;
import ro.cosu.vampires.server.util.SshClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;


public class SshClientResourceTest {
    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                MapBinder<Resource.ProviderType, ResourceProvider> mapbinder
                        = MapBinder.newMapBinder(binder(), Resource.ProviderType.class, ResourceProvider.class);

                mapbinder.addBinding(Resource.ProviderType.SSH).to(SshResourceProvider.class).asEagerSingleton();
            }

            @Provides
            @Named("Config")
            private Config provideConfig() {
                return ConfigFactory.parseString("resources.ssh.local { " +
                        "user= foo\n" +
                        "type= local\n" +
                        "address=bar\n" +
                        "cost=100\n" +
                        "privateKey=baz\n" +
                        "command = foo}");
            }

            @Provides
            @Named("SshClient")
            SshClient provideSsh() throws IOException, JSchException {
                SshClient sshClientMock = Mockito.mock(SshClient.class);
                when(sshClientMock.runCommand(any(), any(), any(), any(), anyInt()))
                        .thenReturn("42");
                return sshClientMock;
            }
        });
    }

    @Test
    public void testStartStopSShResource() throws Exception {
        Resource resource = getResource();
        assertThat(resource.start().get().status(), is(Resource.Status.RUNNING));
        assertThat(resource.stop().get().status(), is(Resource.Status.STOPPED));
    }

    @Test
    public void createSshResource() throws Exception {
        Resource resource = getResource();

        assertThat(resource.status(), equalTo(Resource.Status.SLEEPING));

    }

    private Resource getResource() {
        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider sshProvider = rm.getProviders().get(Resource.ProviderType.SSH);

        Resource.Parameters parameters = sshProvider.getParameters("local");

        return sshProvider.create(parameters).get();
    }

}