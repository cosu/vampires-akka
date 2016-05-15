package ro.cosu.vampires.server.rest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;

public class WorkloadsController extends AbstractRestController<Workload, WorkloadPayload> {

    private static final Logger LOG = LoggerFactory.getLogger(WorkloadsController.class);

    WorkloadsController() {
        super(Workload.class, WorkloadPayload.class, "/workloads");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
