package ro.cosu.vampires.client.allocation;

import java.util.Set;

public interface Allocator {
    Set<Integer> acquireCpuSets(int setSize);
    void releaseCpuSets(Set<Integer> cpuSets);

}
