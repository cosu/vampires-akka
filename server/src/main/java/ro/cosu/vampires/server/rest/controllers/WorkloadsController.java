package ro.cosu.vampires.server.rest.controllers;

import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;

public class WorkloadsController extends AbstractRestController<Workload, WorkloadPayload> {

    WorkloadsController() {
        super(Workload.class, WorkloadPayload.class, "/workloads");
    }
}
