package ro.cosu.vampires.server.settings;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class SettingsImplTest {
    @Test
    public void testSettings() throws Exception {
        SettingsImpl settings = new SettingsImpl(ConfigFactory.load());
        assertThat(settings.getWorkload().size(), not(0));
        assertThat(settings.getWriters(), not(0));

    }
}
