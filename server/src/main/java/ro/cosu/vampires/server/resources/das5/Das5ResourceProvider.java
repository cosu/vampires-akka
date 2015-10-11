package ro.cosu.vampires.server.resources.das5;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

public class Das5ResourceProvider extends AbstractResourceProvider{

    @Override
    public Resource create(Resource.Parameters parameters) {
        if (parameters instanceof Das5ResourceParameters)
            return new Das5Resource((Das5ResourceParameters) parameters);
        else {
            throw  new RuntimeException("invalid parameter type. expected "  + Das5ResourceParameters.class);
        }
    }

    @Override
    public Resource.Type getType() {
        return Resource.Type.DAS5;
    }

    @Override
    public Resource.Parameters.Builder getBuilder() {
        return Das5ResourceParameters.builder();
    }
}
