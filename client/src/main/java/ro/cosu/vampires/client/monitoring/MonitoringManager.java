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

package ro.cosu.vampires.client.monitoring;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import com.codahale.metrics.MetricRegistry;

import org.hyperic.sigar.Sigar;

import java.util.Set;

import kamon.sigar.SigarProvisioner;

public class MonitoringManager {
    private final Set<Source> sources;

    @Inject
    public MonitoringManager(Set<Source> sources) {
        this.sources = sources;
    }

    public static MetricRegistry getMetricRegistry() throws Exception {
        MetricRegistry metricRegistry = new MetricRegistry();

        Injector injector = Guice.createInjector(new MonitoringModule(metricRegistry));

        injector.getInstance(MonitoringManager.class).register();
        return metricRegistry;

    }

    public static Sigar getSigar() throws Exception {
        SigarProvisioner.provision();
        return new Sigar();
    }

//    public static MetricRegistry getMetricRegistry() throws Exception {
//        return getMetricRegistry(getSigar());
//    }

    public void register() {
        sources.stream().forEach(Source::register);
    }
}
