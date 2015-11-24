package ro.cosu.vampires.client.allocation;

import java.util.Optional;

public class NoCpuSetAllocator implements CpuAllocator {
    @Override
    public Optional<CpuSet> acquireCpuSet() {
        return Optional.empty();
    }

    @Override
    public void releaseCpuSets(CpuSet cpuSet) {
        // nothing
    }

    @Override
    public int totalCpuCount() {
        return 0;
    }
}
