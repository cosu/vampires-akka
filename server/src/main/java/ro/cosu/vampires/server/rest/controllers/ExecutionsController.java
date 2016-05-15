package ro.cosu.vampires.server.rest.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionPayload;

public class ExecutionsController extends AbstractRestController<Execution, ExecutionPayload> {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionsController.class);


    private final static String path = "/executions";


    ExecutionsController() {
        super(Execution.class, ExecutionPayload.class, path);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
