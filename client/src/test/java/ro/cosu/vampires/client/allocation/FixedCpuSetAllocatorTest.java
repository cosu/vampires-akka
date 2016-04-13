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

import org.junit.Test;

import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FixedCpuSetAllocatorTest {

    @Test
    public void testAcquireCpuSets() throws Exception {
        final FixedCpuSetAllocator fixedCpuSetAllocator = FixedCpuSetAllocator.builder().cpuSetSize(4).totalCpuCount
                (8).build();

        final CpuSet cpuSet = fixedCpuSetAllocator.acquireCpuSet().get();

        assertThat(cpuSet.getCpuSet().size(), is(4));


    }


    @Test
    public void testTakeAfterRelease() throws Exception {
        final FixedCpuSetAllocator fixedCpuSetAllocator = FixedCpuSetAllocator.builder().cpuSetSize(4).totalCpuCount
                (8).build();

        IntStream.iterate(0, i -> i + 1)
                .limit(10)
                .parallel()
                .forEach(i ->
                {
                    CpuSet acquireCpuSets = fixedCpuSetAllocator.acquireCpuSet().get();
                    fixedCpuSetAllocator.releaseCpuSets(acquireCpuSets);

                });


        final Optional<CpuSet> acquireCpuSets = fixedCpuSetAllocator.acquireCpuSet();
        assertThat(acquireCpuSets.get().getCpuSet().size(), is(4));
    }
}
