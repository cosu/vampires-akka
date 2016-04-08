package ro.cosu.vampires.server.resources.das5;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.util.SshClient;

public class Das5ResourceProvider extends AbstractResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(Das5ResourceProvider.class);

    @Inject
    @Named("DASSSH")
    private SshClient sshClient;

    @Override
    public Optional<Resource> create(Resource.Parameters parameters) {

        if (parameters instanceof Das5ResourceParameters)
            return Optional.of(new Das5Resource((Das5ResourceParameters) parameters, sshClient));
        else {
            LOG.error("invalid parameter type. expected " + Das5ResourceParameters.class);
            return Optional.empty();
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
