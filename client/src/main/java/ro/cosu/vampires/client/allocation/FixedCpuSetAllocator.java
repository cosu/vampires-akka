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

package ro.cosu.vampires.client.allocation;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class FixedCpuSetAllocator implements CpuAllocator {
    /**
     * Emits CPUSets to be used by numactl or docker cpusets This uses a round robin strategy w.r.t
     * to releasing and acquiring
     */
    private static final Logger LOG = LoggerFactory.getLogger(FixedCpuSetAllocator.class);
    private final int totalCpuCount;

    private BlockingDeque<CpuSet> cpuList = Queues.newLinkedBlockingDeque();


    public FixedCpuSetAllocator(Builder builder) {
        final List<Integer> integerList = IntStream.iterate(0, i -> i + 1)
                .boxed().limit(builder.totalCpuCount)
                .collect(Collectors.toList());


        Lists.partition(integerList, builder.cpuSetSize).stream().map(HashSet::new).map(CpuSet::new).forEach
                (cpuList::addLast);

        this.totalCpuCount = builder.totalCpuCount;

    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Optional<CpuSet> acquireCpuSet() {
        CpuSet cpuSet = null;
        try {
            cpuSet = cpuList.takeFirst();
            LOG.debug("take {}", cpuSet);
        } catch (InterruptedException e) {
            LOG.debug("{}");
        }
        return Optional.ofNullable(cpuSet);

    }

    @Override
    public void releaseCpuSets(CpuSet cpuSet) {
        LOG.debug("put {}", cpuSet);
        cpuList.addLast(cpuSet);

    }

    @Override
    public int totalCpuCount() {
        return totalCpuCount;
    }

    public static class Builder {
        private int cpuSetSize;
        private int totalCpuCount;

        public Builder totalCpuCount(int totalCpuCount) {
            this.totalCpuCount = totalCpuCount;
            return this;
        }

        public Builder cpuSetSize(int cpuSetSize) {
            this.cpuSetSize = cpuSetSize;
            return this;
        }

        public FixedCpuSetAllocator build() {
            return new FixedCpuSetAllocator(this);
        }

    }
}
