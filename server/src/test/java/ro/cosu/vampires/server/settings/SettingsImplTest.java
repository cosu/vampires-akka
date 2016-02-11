package ro.cosu.vampires.server.settings;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.server.writers.ResultsWriter;
import ro.cosu.vampires.server.writers.mongo.MongoWriter;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class SettingsImplTest {
    @Test
    public void testSettings() throws Exception {
        SettingsImpl settings = new SettingsImpl(ConfigFactory.load());
        assertThat(settings.getWorkload().size(), not(0));
        assertThat(settings.getWriters().size(), not(0));

    }

    @Test
    public void testGetMongoWriter() throws Exception {

        Config config = ConfigFactory.parseString("vampires.enabled-writers = [\"mongo\"]").withFallback(ConfigFactory.load());

        SettingsImpl settings = new SettingsImpl(config);

        assertThat(settings.getWriters().size(), not(0));
        ResultsWriter writer = settings.getWriters().get(0);

        assertThat(writer instanceof MongoWriter, is(true));
    }

    @Test
    public void testGetCpuSetSize() throws Exception {

        Config config = ConfigFactory.parseString("vampires.cpuSetSize=4").withFallback(ConfigFactory.load());

        SettingsImpl settings = new SettingsImpl(config);
        assertThat(settings.getCpuSetSize(), is(4));
    }

    @Test
    public void testNoCpuSetSize(){
        Config config = ConfigFactory.parseString("vampires.cpuSetSize = null").withFallback(ConfigFactory.load());
        SettingsImpl settings = new SettingsImpl(config);

        assertThat(settings.getCpuSetSize(), is(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoExecutors(){
        Config config = ConfigFactory.parseString("vampires.executors = null").withFallback(ConfigFactory.load());
        SettingsImpl settings = new SettingsImpl(config);

        assertThat(settings.getExecutors().size(), is(0));
    }
}
