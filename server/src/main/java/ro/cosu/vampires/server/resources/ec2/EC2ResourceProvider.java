package ro.cosu.vampires.server.resources.ec2;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;

public class EC2ResourceProvider extends AbstractResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(EC2ResourceProvider.class);

    @Inject
    @Nullable
    private AmazonEC2Client amazonEC2Client;

    @Override
    public Optional<Resource> create(Resource.Parameters parameters) {
        if (amazonEC2Client == null) {
            throw new IllegalArgumentException("unable to get ec2client instance");
        }
        if (parameters instanceof EC2ResourceParameters)
            return Optional.of(new EC2Resource((EC2ResourceParameters) parameters, amazonEC2Client));
        else {
            LOG.error("invalid parameter type. expected " + EC2ResourceParameters.class);
        }
        LOG.error("Failed to create amazon ec2 resource");
        return Optional.empty();
    }

    @Override
    public Resource.Type getType() {
        return Resource.Type.EC2;
    }

    @Override
    public Resource.Parameters.Builder getBuilder() {
        return EC2ResourceParameters.builder();
    }

    @Override
    protected Config getSimpleConfigForInstance(String instanceName) {
        if (getConfig().hasPath(getInstanceKey(instanceName))) {
            return super.getSimpleConfigForInstance(instanceName);
        }
        return parseInstanceType(instanceName);
    }

    private Config parseInstanceType(String type) {
        Map<String, String> map = Maps.newHashMap();
        String[] split = type.split("\\.");
        Preconditions.checkArgument(split.length == 3,
                "invalid instance format. Should be <region>.<class>.<name>. Eg: eu-west-1.t2.micro");
        map.put("region", split[0]);
        map.put("instanceType", Joiner.on(".").join(split[1], split[2]));

        return ConfigFactory.parseMap(map);
    }
}