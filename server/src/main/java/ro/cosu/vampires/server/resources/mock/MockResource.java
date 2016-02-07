package ro.cosu.vampires.server.resources.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;


public class MockResource extends AbstractResource {

    private static final Logger LOG = LoggerFactory.getLogger(MockResource.class);
    private final MockResourceParameters parameters;

    public MockResource(Parameters parameters) {
        super(parameters);
        this.parameters = (MockResourceParameters) parameters;

    }

    @Override
    public void onStart() throws Exception {
        LOG.debug("mock start: {}", parameters);
        if (parameters.command().equals("fail")) {
            throw new RuntimeException("fail");
        }
    }

    @Override
    public void onStop() throws Exception {

    }

    @Override
    public void onFail() throws Exception {

    }
}
