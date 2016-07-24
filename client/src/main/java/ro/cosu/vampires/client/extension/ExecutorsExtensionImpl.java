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

package ro.cosu.vampires.client.extension;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import akka.actor.Extension;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.executors.docker.DockerExecutorModule;
import ro.cosu.vampires.client.executors.fork.ForkExecutorModule;
import ro.cosu.vampires.server.values.ClientConfig;

public class ExecutorsExtensionImpl implements Extension {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorsExtensionImpl.class);
    public Config vampires;
    private Injector injector;
    private Map<String, Integer> executorInfo = new HashMap<>();

    public ExecutorsExtensionImpl(Config config) {
        vampires = config.getConfig("vampires");

        try {
            injector = Guice.createInjector(new DockerExecutorModule(vampires));
            final Executor executor = injector.getInstance(Executor.class);
            Preconditions.checkArgument(executor.isAvailable());
            executorInfo.put(executor.getType().toString(), executor.getNCpu());
        } catch (Exception e) {
            LOG.info("docker executor not available");
        }

        try {
            injector = Guice.createInjector(new ForkExecutorModule(vampires));
            final Executor executor = injector.getInstance(Executor.class);
            executorInfo.put(executor.getType().toString(), executor.getNCpu());
        } catch (Exception e) {
            LOG.info("fork executor not available");
        }

        LOG.info("available executors: {}", executorInfo);

    }

    public Executor getExecutor() {
        return injector.getInstance(Executor.class);
    }

    public void configure(ClientConfig config) {

        final Executor.Type executor = Executor.Type.valueOf(config.executor());

        vampires = ConfigFactory.parseString("cpuSetSize=" + config.cpuSetSize()).withFallback(vampires);

        if (executor.equals(Executor.Type.DOCKER)) {
            injector = Guice.createInjector(new DockerExecutorModule(vampires));
        }

        if (executor.equals(Executor.Type.FORK)) {
            injector = Guice.createInjector(new ForkExecutorModule(vampires));
        }

        LOG.info("configured {}", config);

        Preconditions.checkArgument(getExecutor().isAvailable());

    }

    public Map<String, Integer> getExecutorInfo() {
        return executorInfo;
    }
}


