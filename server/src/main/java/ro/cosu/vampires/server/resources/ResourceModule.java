package ro.cosu.vampires.server.resources;

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
import ro.cosu.vampires.server.resources.das5.Das5ResourceProvider;
import ro.cosu.vampires.server.resources.ec2.EC2ResourceProvider;
import ro.cosu.vampires.server.resources.local.LocalResourceProvider;
import ro.cosu.vampires.server.resources.ssh.SshResourceProvider;
import ro.cosu.vampires.server.util.Ssh;

import java.io.FileInputStream;
import java.io.IOException;

public class ResourceModule extends AbstractModule{
    private Config config;
    static final Logger LOG = LoggerFactory.getLogger(ResourceModule.class);


    public ResourceModule(Config config) {
        this.config = config;
    }


    @Override
    protected void configure() {
        MapBinder<Resource.Type, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);
        mapbinder.addBinding(Resource.Type.SSH).to(SshResourceProvider.class).asEagerSingleton();
        mapbinder.addBinding(Resource.Type.LOCAL).to(LocalResourceProvider.class).asEagerSingleton();
        mapbinder.addBinding(Resource.Type.DAS5).to(Das5ResourceProvider.class).asEagerSingleton();
        mapbinder.addBinding(Resource.Type.EC2).to(EC2ResourceProvider.class).in(Scopes.SINGLETON);

    }

    @Provides
    @Named("Config")
    Config provideConfig(){
        return this.config;
    }

    @Provides
    Ssh provideSsh (){
        return new Ssh();
    }

    @Provides
    AmazonEC2Client provideAmazonEc2(){

        String credentialsFile = config.getString("resources.ec2.credentialsFile");
        LOG.info("reading credentials  AWS from {}", credentialsFile);
        try {
            PropertiesCredentials credentials = new PropertiesCredentials(new FileInputStream(credentialsFile));
            return new AmazonEC2Client(credentials);
        } catch (IOException e) {
            LOG.error("failed to create amazon client {}", e);
        }
        return null;
    }

}
