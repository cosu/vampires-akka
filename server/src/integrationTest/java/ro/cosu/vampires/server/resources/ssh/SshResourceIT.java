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

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceModule;
import ro.cosu.vampires.server.resources.ResourceProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class SshResourceIT {
    @Test
    public void testSshResoure() throws Exception {
        Injector injector = Guice.createInjector(new ResourceModule(ConfigFactory.load().getConfig("vampires")));


        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider sshProvider = rm.getProviders().get(Resource.ProviderType.SSH);
        Resource.Parameters parameters = sshProvider.getParameters("local");

        Resource resource = sshProvider.create(parameters).get();

        assertThat(resource.status(), equalTo(Resource.Status.SLEEPING));

    }

}
