package ro.cosu.vampires.server.resources.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

import java.util.Optional;

public class LocalResourceProvider extends AbstractResourceProvider{

    static final Logger LOG = LoggerFactory.getLogger(LocalResourceProvider.class);

    @Override
    public Optional<Resource> create(Resource.Parameters parameters) {
        if (parameters instanceof LocalResourceParameters)
            return Optional.of(new LocalResource((LocalResourceParameters) parameters));
        else {
            LOG.error("invalid parameter type. expected " + LocalResourceProvider.class);
            return Optional.empty();
        }
    }


    @Override
    public Resource.Parameters.Builder getBuilder(){
        return LocalResourceParameters.builder();
    }

    @Override
    public Resource.Type getType() {
        return Resource.Type.LOCAL;
    }
}
