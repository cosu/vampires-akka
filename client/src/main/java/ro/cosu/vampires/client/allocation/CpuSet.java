package ro.cosu.vampires.client.allocation;

import java.util.Set;

public class CpuSet {
    private Set<Integer> cpuSet ;

    CpuSet(Set<Integer> cpuSet){
        this.cpuSet = cpuSet;
    }

    @Override
    public String toString() {
        return "CpuSet{" +
                "cpuSet=" + cpuSet +
                '}';
    }

    public Set<Integer> getCpuSet() {
        return cpuSet;
    }
}
