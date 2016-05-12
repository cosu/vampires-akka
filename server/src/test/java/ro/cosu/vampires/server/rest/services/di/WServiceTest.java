package ro.cosu.vampires.server.rest.services.di;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import com.typesafe.config.ConfigFactory;

import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;

public class WServiceTest extends AbstractDiTest<Workload, WorkloadPayload> {

    @Override
    protected AbstractModule getModule() {
        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(getTypeTokenService()).to(new TypeLiteral<WService>() {
                });
            }
        };
        return module;
    }

    @Override
    protected TypeLiteral<Service<Workload, WorkloadPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Workload, WorkloadPayload>>() {
        };
    }

    @Override
    protected WorkloadPayload getPayload() {

        String workloadConfig = "{\n" +
                "    format = %08d.tif\n" +
                "    sequenceStart = 0\n" +
                "    sequenceStop = 10\n" +
                "    task = \"echo\"\n" +
                "    url = \"\"\n" +
                "  }";

        return WorkloadPayload.fromConfig(ConfigFactory.parseString(workloadConfig));
    }
}