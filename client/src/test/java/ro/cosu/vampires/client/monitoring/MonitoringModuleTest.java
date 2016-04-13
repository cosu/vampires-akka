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

package ro.cosu.vampires.client.monitoring;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import kamon.sigar.SigarProvisioner;
import org.hyperic.sigar.Sigar;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class MonitoringModuleTest {
    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    @Test
    public void testMonitoring() throws Exception {
        SigarProvisioner.provision();
        Sigar sigar = new Sigar();
        MetricRegistry metricRegistry = new MetricRegistry();

        Injector injector = Guice.createInjector(new MonitoringModule(metricRegistry, sigar));

        injector.getInstance(MonitoringManager.class).register();

        assertThat(metricRegistry.getGauges().values().iterator().next().getValue(), notNullValue());


    }
}
