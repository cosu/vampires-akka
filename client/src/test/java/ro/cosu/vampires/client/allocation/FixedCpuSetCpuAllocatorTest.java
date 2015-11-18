package ro.cosu.vampires.client.allocation;

import org.junit.Test;

import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FixedCpuSetCpuAllocatorTest {

    @Test
    public void testAcquireCpuSets() throws Exception {
        final FixedCpuSetAllocator fixedCpuSetAllocator = FixedCpuSetAllocator.builder().cpuSetSize (4).totalCpuCount
                (8).build();

        final CpuSet cpuSet = fixedCpuSetAllocator.acquireCpuSet().get();

        assertThat(cpuSet.getCpuSet().size(), is(4));


    }


    @Test
    public void testTakeAfterRelease() throws Exception {
        final FixedCpuSetAllocator fixedCpuSetAllocator = FixedCpuSetAllocator.builder().cpuSetSize (4).totalCpuCount
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
