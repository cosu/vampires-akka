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

/*
 * Created by IntelliJ IDEA.
 * User: cdumitru
 * Date: 25/10/15
 * Time: 23:51
 */
package ro.cosu.vampires.client.executors;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.cosu.vampires.client.allocation.CpuAllocator;
import ro.cosu.vampires.client.allocation.FixedCpuSetAllocator;

public abstract class AbstractExecutorModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractExecutorModule.class);

    private final Config config;

    public AbstractExecutorModule(Config config) {
        this.config = config;
    }

    @Provides
    @Named("Config")
    private Config provideConfig() {
        return this.config;
    }

    protected abstract void configure();

    @Provides
    @Singleton
    private CpuAllocator provideCpuAllocator(@Named("cpuCount") int cpuCount) {
        int cpuSetSize = config.hasPath("cpu-set-size") ? config.getInt("cpu-set-size") : 1;

        LOG.info(" cpuCount: {} countSetSize: {}", cpuCount, cpuSetSize);

        return FixedCpuSetAllocator.builder()
                .cpuSetSize(cpuSetSize)
                .totalCpuCount(cpuCount)
                .build();

    }


}
