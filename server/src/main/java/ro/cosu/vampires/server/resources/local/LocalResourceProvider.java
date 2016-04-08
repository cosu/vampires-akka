package ro.cosu.vampires.server.resources.local;

import com.google.inject.Inject;

import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

public class LocalResourceProvider extends AbstractResourceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LocalResourceProvider.class);

    @Inject
    private Executor executor;

    @Override
    public Optional<Resource> create(Resource.Parameters parameters) {
        if (parameters instanceof LocalResourceParameters)
            return Optional.of(new LocalResource((LocalResourceParameters) parameters, executor));
        else {
            LOG.error("invalid parameter type. expected " + LocalResourceProvider.class);
            return Optional.empty();
        }
    }


    @Override
    public Resource.Parameters.Builder getBuilder() {
        return LocalResourceParameters.builder();
    }

    @Override
    public Resource.Type getType() {
        return Resource.Type.LOCAL;
    }
}
