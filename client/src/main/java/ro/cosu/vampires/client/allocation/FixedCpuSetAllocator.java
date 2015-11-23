package ro.cosu.vampires.client.allocation;

import autovalue.shaded.com.google.common.common.collect.Lists;
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
     * Emits CPUSets to be used by numactl or docker cpusets This uses a round robin strategy w.r.t to releasing and
     * acquiring
     */
    static final Logger LOG = LoggerFactory.getLogger(FixedCpuSetAllocator.class);

    BlockingDeque<CpuSet> cpuList = Queues.newLinkedBlockingDeque();


    public FixedCpuSetAllocator(Builder builder) {
        final List<Integer> integerList = IntStream.iterate(0, i -> i + 1)
                .boxed().limit(builder.totalCpuCount)
                .collect(Collectors.toList());


        Lists.partition(integerList, builder.cpuSetSize).stream().map(HashSet::new).map(CpuSet::new).forEach
                (cpuList::addLast);

    }

    public  static Builder builder(){
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

        public  FixedCpuSetAllocator build() {
            return new FixedCpuSetAllocator(this);
        }

    }
}
