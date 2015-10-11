package ro.cosu.vampires.server.resources.ssh;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class BuilderTest {

    @Test
    public void testFromConfig() throws Exception {

        Config config = ConfigFactory.load();

        Config sshConfig = config.getConfig("vampires.resources.ssh.instances");
        Config localSshConfig = config.getConfig("vampires.resources.ssh.instances.local").withFallback(sshConfig);

        SshResourceParameters params = SshResourceParameters.builder().fromConfig(localSshConfig).build();

        assertThat(params.address(), equalTo("localhost"));

    }
}
