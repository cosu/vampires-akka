package ro.cosu.vampires.client.monitoring;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codahale.metrics.MetricRegistry.name;

public class CpuSource implements Source {

    private static final Logger LOG = LoggerFactory.getLogger(CpuSource.class);

    private static String NAME = "cpu";

    @Inject
    Sigar sigar;
    @Inject
    MetricRegistry metricRegistry;

    public void register() {
        metricRegistry.register(name(getName(), "cpu-total-usage"), this.cpuTotalUsageGague());
        metricRegistry.register(name(getName(), "cpu-wait-usage"), this.cpuWaitUsageGague());
        metricRegistry.register(name(getName(), "cpu-user-usage"), this.cpuUserUsageGague());
        metricRegistry.register(name(getName(), "load-one-minute-average"), this.oneMinLoadGague());
        metricRegistry.register(name(getName(), "load-five-minute-average"), this.fiveMinLoadGague());
        metricRegistry.register(name(getName(), "load-fifteen-minute-average"), this
                .fifteenMinLoadGague());


        LOG.debug("cpu network source");
    }

    @Override
    public String getName() {
        return NAME;
    }


    private Gauge<Double> cpuUserUsageGague() {
        return () -> {
            try {
                CpuPerc cpuPc = sigar.getCpuPerc();
                return round(cpuPc.getUser());
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<Double> cpuWaitUsageGague() {
        return () -> {
            try {
                CpuPerc cpuPc = sigar.getCpuPerc();
                return round(cpuPc.getWait());
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<Double> cpuTotalUsageGague() {
        return () -> {
            try {
                CpuPerc cpuPc = sigar.getCpuPerc();
                return round(cpuPc.getCombined());
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }


    private Gauge<Double> oneMinLoadGague() {
        return () -> {
            try {
                double[] la = sigar.getLoadAverage();
                return la[0];
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<Double> fiveMinLoadGague() {
        return () -> {
            try {

                double[] la = sigar.getLoadAverage();
                return la[1];
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }

    private Gauge<Double> fifteenMinLoadGague() {
        return () -> {
            try {
                double[] la = sigar.getLoadAverage();
                return la[2];
            } catch (SigarException e) {
                LOG.error("cpu metric register failed ", e);
            }
            return null;
        };
    }


    private static double round(double d) {
        return (((double) Math.round(d * 1000)) / 1000D);
    }

}
