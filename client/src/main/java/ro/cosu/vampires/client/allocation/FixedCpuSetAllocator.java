package ro.cosu.vampires.client.allocation;

import autovalue.shaded.com.google.common.common.collect.Sets;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.google.common.primitives.Ints.min;

public class FixedCpuSetAllocator implements Allocator {
    /**
     * Emits CPUSets to be used by numactl or docker cpusets This uses a round robin strategy w.r.t to releasing and
     * acquiring
     */
    static final Logger LOG = LoggerFactory.getLogger(FixedCpuSetAllocator.class);

    BlockingDeque<Integer> cpuList = Queues.newLinkedBlockingDeque();
    final private int size;


    FixedCpuSetAllocator(int size) {
        this.size = size;
        IntStream.iterate(0, i -> i + 1).limit(size).forEachOrdered(cpuList::addFirst);

    }

    @Override
    public Set<Integer> acquireCpuSets(int cpuSetSize) {
        int cpusToTake = min(cpuSetSize, size);

        final HashSet<Integer> cpus = Sets.newHashSet();
        try {
            Queues.drain(cpuList, cpus, cpusToTake, 60, TimeUnit.SECONDS);
            LOG.debug("Acquired {}", cpus);
        } catch (InterruptedException e) {
            LOG.error("{}", e);
        }
        return cpus;
    }

    @Override
    public void releaseCpuSets(Set<Integer> cpuSets) {
        int maxCapacity = min(cpuSets.size(), size);

        cpuSets.stream().limit(maxCapacity).forEachOrdered(cpuList::addLast);
    }
}
