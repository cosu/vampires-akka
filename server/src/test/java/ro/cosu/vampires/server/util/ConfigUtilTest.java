package ro.cosu.vampires.server.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ConfigUtilTest {

    @Test
    public void noNeedtoFallbackTest() throws Exception {
        String configString = "  resources {\n" +
                "    privateKey = resources key\n" +
                "    ssh {\n" +
                "      privateKey = ssh resource key\n" +
                "      local {\n" +
                "        privateKey = local key\n" +
                "      }\n" +
                "    }" +
                "   }";

        Config config = ConfigFactory.parseString(configString);
        Config resources = ConfigUtil.getConfigForKey(config, "resources", "ssh", "local");
        assertThat(resources.getString("privateKey"), is("local key"));
    }

    @Test
    public void oneLevelFallbackTest() throws Exception {
        String configString = "  resources {\n" +
                "    privateKey = resources key\n" +
                "    ssh {\n" +
                "      privateKey = ssh resource key\n" +
                "      local {\n" +
                "      }\n" +
                "    }" +
                "   }";

        Config config = ConfigFactory.parseString(configString);
        Config resources = ConfigUtil.getConfigForKey(config, "resources", "ssh", "local");
        assertThat(resources.getString("privateKey"), is("ssh resource key"));
    }

    @Test
    public void twoLevelFallbackTest() throws Exception {
        String configString = "  resources {\n" +
                "    privateKey = resources key\n" +
                "    ssh {\n" +
                "      local {\n" +
                "      }\n" +
                "    }" +
                "   }";

        Config config = ConfigFactory.parseString(configString);
        Config resources = ConfigUtil.getConfigForKey(config, "resources", "ssh", "local");
        assertThat(resources.getString("privateKey"), is("resources key"));
    }
}


