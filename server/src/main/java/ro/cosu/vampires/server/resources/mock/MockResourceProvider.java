package ro.cosu.vampires.server.resources.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

public class MockResourceProvider extends AbstractResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MockResourceProvider.class);

    @Override
    public Optional<Resource> create(Resource.Parameters parameters) {

        if (parameters instanceof MockResourceParameters)
            return Optional.of(new MockResource(parameters));
        else {
            LOG.error("invalid parameter type. expected " + MockResourceProvider.class);
            return Optional.empty();
        }

    }

    @Override
    public Resource.Type getType() {
        return Resource.Type.MOCK;
    }

    @Override
    public Resource.Parameters.Builder getBuilder() {
        return MockResourceParameters.builder();
    }

}
