package ro.cosu.vampires.server.rest.controllers;


import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionPayload;

public class ExecutionsController extends AbstractRestController<Execution, ExecutionPayload> {
    ExecutionsController() {
        super(Execution.class, ExecutionPayload.class, "/executions");
    }
}
