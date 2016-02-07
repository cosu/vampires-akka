package ro.cosu.vampires.server.resources;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import ro.cosu.vampires.server.resources.das5.Das5ResourceModule;
import ro.cosu.vampires.server.resources.ec2.EC2ResourceModule;
import ro.cosu.vampires.server.resources.local.LocalResourceModule;
import ro.cosu.vampires.server.resources.mock.MockResourceModule;
import ro.cosu.vampires.server.resources.ssh.SshResourceModule;

public class ResourceModule extends AbstractModule{
    private Config config;

    public ResourceModule(Config config) {
        this.config = config;
    }

    @Override
    protected void configure() {

        install(new Das5ResourceModule());
        install(new LocalResourceModule());
        install(new EC2ResourceModule());
        install(new SshResourceModule());
        install(new MockResourceModule());

    }

    @Provides @Named("Config")
    private Config provideConfig(){
        return this.config;
    }





}
