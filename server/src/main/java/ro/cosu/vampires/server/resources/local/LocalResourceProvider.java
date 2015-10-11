package ro.cosu.vampires.server.resources.local;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

public class LocalResourceProvider extends AbstractResourceProvider{


    @Override
    public Resource create(Resource.Parameters parameters) {
        if (parameters instanceof LocalResourceParameters)
            return new LocalResource((LocalResourceParameters) parameters);
        else {
            throw  new RuntimeException("invalid parameter type. expected "  + LocalResourceParameters.class);
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
