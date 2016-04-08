package ro.cosu.vampires.client.monitoring;

public class HostInfo {
    public static int getAvailableProcs() {
        return Runtime.getRuntime().availableProcessors();
    }
}
