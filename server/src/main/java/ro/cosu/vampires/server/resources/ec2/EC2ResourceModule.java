package ro.cosu.vampires.server.resources.ec2;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

public class EC2ResourceModule extends AbstractModule {
    static final Logger LOG = LoggerFactory.getLogger(EC2ResourceModule.class);

    @Override
    protected void configure() {
        MapBinder<Resource.Type, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);
        mapbinder.addBinding(Resource.Type.EC2).to(EC2ResourceProvider.class).in(Scopes.SINGLETON);
    }

    @Provides
    Optional<AmazonEC2Client> provideAmazonEc2(@Named("Config") Config config) {

        AmazonEC2Client amazonEC2Client = null;


        if (config.hasPath("resources.ec2.credentialsFile")) {
            String credentialsFile = config.getString("resources.ec2.credentialsFile");
            LOG.info("reading credentials  AWS from {}", credentialsFile);
            try {
                PropertiesCredentials credentials = new PropertiesCredentials(new FileInputStream(credentialsFile));
                amazonEC2Client = new AmazonEC2Client(credentials);
            } catch (IOException e) {
                LOG.error("failed to create amazon client", e);
            }
        }

        return Optional.ofNullable(amazonEC2Client);
    }
}
