package ro.cosu.vampires.server.resources.ssh;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.util.SshClient;

public class SshResourceProvider extends AbstractResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SshResourceProvider.class);

    @Inject
    @Named("SshClient")
    private SshClient sshClient;

    @Override
    public Optional<Resource> create(Resource.Parameters parameters) {
        if (parameters instanceof SshResourceParameters)
            return Optional.of(new SshResource((SshResourceParameters) parameters, sshClient));
        else {

            LOG.error("invalid parameter type. expected " + SshResourceParameters.class + " but got " + parameters
                    .getClass().getName());
            return Optional.empty();
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
