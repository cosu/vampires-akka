package ro.cosu.vampires.client.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostInfo {
    static final Logger LOG = LoggerFactory.getLogger(HostInfo.class);
    public static int getParallel(){
        return Runtime.getRuntime().availableProcessors();
    }
}
