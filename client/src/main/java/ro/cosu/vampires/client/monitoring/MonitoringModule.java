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
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.hyperic.sigar.Sigar;

public class MonitoringModule extends AbstractModule {

    private final MetricRegistry metricRegistry;
    private final Sigar sigar;

    public MonitoringModule(MetricRegistry metricRegistry, Sigar sigar) {

        this.metricRegistry = metricRegistry;
        this.sigar = sigar;
    }

    @Override
    protected void configure() {

        Multibinder<Source> sourceMultibinder = Multibinder.newSetBinder(binder(), Source.class);

        sourceMultibinder.addBinding().to(CpuSource.class).asEagerSingleton();
        sourceMultibinder.addBinding().to(NetworkSource.class).asEagerSingleton();
        sourceMultibinder.addBinding().to(HostSource.class).asEagerSingleton();

        bind(Sigar.class).toInstance(sigar);
        bind(MetricRegistry.class).toInstance(metricRegistry);
    }
}
