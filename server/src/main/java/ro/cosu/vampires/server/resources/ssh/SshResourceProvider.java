package ro.cosu.vampires.server.resources.ssh;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

public class SshResourceProvider  extends AbstractResourceProvider {

    @Override
    public Resource create(Resource.Parameters parameters) {
        if (parameters instanceof SshResourceParameters)
            return new SshResource((SshResourceParameters) parameters);
        else {
            throw  new RuntimeException("invalid parameter type. expected "  + SshResourceParameters.class);
        }
    }

    @Override
    public Resource.Type getType() {
        return Resource.Type.SSH;
    }

    @Override
    public Resource.Parameters.Builder getBuilder() {
        return SshResourceParameters.builder();
    }
}
