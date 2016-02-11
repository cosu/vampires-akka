package ro.cosu.vampires.server.resources.ec2;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.mockito.Mockito;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceManager;
import ro.cosu.vampires.server.resources.ResourceProvider;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


public class EC2ResourceTest {
    @Test
    public void createEC2Resource() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                MapBinder<Resource.Type, ResourceProvider> mapbinder
                        = MapBinder.newMapBinder(binder(), Resource.Type.class, ResourceProvider.class);

                mapbinder.addBinding(Resource.Type.EC2).to(EC2ResourceProvider.class).asEagerSingleton();
            }
            @Provides
            @Named("Config")
            private Config provideConfig(){
                return ConfigFactory.parseString("resources.ec2.local { " +
                        "command=bar\n"+
                        "imageId=baz\n"+
                        "instanceType= foo\n" +
                        "keyName=foo\n" +
                        "securityGroup=foo\n"+
                        "region=eu\n" +
                        "}"
                        );
            }
            @Provides
            private Optional<AmazonEC2Client> provideAmazonEc2(@Named("Config") Config config) {
                return Optional.of(Mockito.mock(AmazonEC2Client.class));
            }
            });

        ResourceManager rm = injector.getInstance(ResourceManager.class);

        ResourceProvider ec2Provider = rm.getProviders().get(Resource.Type.EC2);
        Resource.Parameters parameters = ec2Provider.getParameters("local");

        Resource resource = ec2Provider.create(parameters).get();

        assertThat(resource.status(), equalTo(Resource.Status.SLEEPING));

    }

}