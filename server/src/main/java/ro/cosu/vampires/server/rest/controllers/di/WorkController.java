package ro.cosu.vampires.server.rest.controllers.di;

import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;

public class WorkController extends AbstractRestController<Workload, WorkloadPayload> {

    WorkController() {
        super(Workload.class, WorkloadPayload.class);
    }
}
