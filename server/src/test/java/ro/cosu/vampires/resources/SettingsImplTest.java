package ro.cosu.vampires.resources;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.server.settings.SettingsImpl;

public class SettingsImplTest {
    @Test
    public void testGetStart() throws Exception {
        SettingsImpl settings = new SettingsImpl(ConfigFactory.load());
//        settings.getStartResources();

    }
}
