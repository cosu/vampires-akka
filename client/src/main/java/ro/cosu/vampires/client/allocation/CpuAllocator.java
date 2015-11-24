package ro.cosu.vampires.client.allocation;

import java.util.Optional;

public interface CpuAllocator {

    Optional<CpuSet> acquireCpuSet();

    void releaseCpuSets(CpuSet cpuSet);

    int totalCpuCount();
}
