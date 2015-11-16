package ro.cosu.vampires.client.allocation;

import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FixedCpuSetAllocatorTest {

    @Test
    public void testAcquireCpuSets() throws Exception {

        final FixedCpuSetAllocator fixedCpuSetAllocator = new FixedCpuSetAllocator(10);

        final Set<Integer> cpuSet= fixedCpuSetAllocator.acquireCpuSets(10);

        assertThat(cpuSet.size(), is(10));

    }

    @Test
    public void testAcquireReleaseAcquire() throws Exception {
        final FixedCpuSetAllocator fixedCpuSetAllocator = new FixedCpuSetAllocator(10);

        fixedCpuSetAllocator.releaseCpuSets(fixedCpuSetAllocator.acquireCpuSets(5));
        final Set<Integer> cpuSet= fixedCpuSetAllocator.acquireCpuSets(10);

        assertThat(cpuSet.size(), is(10));

    }

    @Test
    public void testTakeAfterRelease() throws Exception {
        final FixedCpuSetAllocator fixedCpuSetAllocator = new FixedCpuSetAllocator(10);
        final Set<Integer> initialCpuSet = fixedCpuSetAllocator.acquireCpuSets(5);

        final CompletableFuture<Set<Integer>> futureCpuSets = CompletableFuture.supplyAsync(() ->
                fixedCpuSetAllocator.acquireCpuSets(6)
        );

        fixedCpuSetAllocator.releaseCpuSets(initialCpuSet.stream().limit(2).collect(Collectors.toSet()));

        final Set<Integer> acquiredCpus = futureCpuSets.get();

        assertThat(acquiredCpus.size(), is(6));


        System.out.println(acquiredCpus);
    }
}
