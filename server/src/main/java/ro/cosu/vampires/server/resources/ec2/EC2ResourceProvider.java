package ro.cosu.vampires.server.resources.ec2;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

import java.util.Optional;

public class EC2ResourceProvider extends AbstractResourceProvider {
    static final Logger LOG = LoggerFactory.getLogger(EC2ResourceProvider.class);

    @Inject
    AmazonEC2Client amazonEC2Client;




    @Override
    public Optional<Resource> create(Resource.Parameters parameters) {
        if (parameters instanceof EC2ResourceParameters)
            return Optional.of(new EC2Resource((EC2ResourceParameters) parameters, amazonEC2Client));
        else {
            LOG.error("invalid parameter type. expected " + EC2ResourceParameters.class);
            return Optional.empty();
        }
    }

    @Override
    public Resource.Type getType() {
        return Resource.Type.EC2;
    }

    @Override
    public Resource.Parameters.Builder getBuilder() {
            return EC2ResourceParameters.builder();

    }
}
