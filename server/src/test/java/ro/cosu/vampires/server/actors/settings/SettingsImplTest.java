/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.actors.settings;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import ro.cosu.vampires.server.writers.ResultsWriter;
import ro.cosu.vampires.server.writers.mongo.MongoWriter;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class SettingsImplTest {
    private String CONF_FILE = "application-dev.conf";
    private Config devConfig = ConfigFactory.load(CONF_FILE);
    @Test
    public void testSettings() throws Exception {
        SettingsImpl settings = new SettingsImpl(ConfigFactory.load(CONF_FILE));
        assertThat(settings.getWorkload().size(), not(0));

    }

    @Test
    public void testGetMongoWriter() throws Exception {

        Config config = ConfigFactory.parseString("vampires.enabled-writers = [\"mongo\"]").withFallback(devConfig);

        SettingsImpl settings = new SettingsImpl(config);

        assertThat(settings.getWriters().size(), not(0));
        ResultsWriter writer = settings.getWriters().get(0);

        assertThat(writer instanceof MongoWriter, is(true));
    }

    @Test
    public void testGetCpuSetSize() throws Exception {

        Config config = ConfigFactory.parseString("vampires.cpu-set-size=4").withFallback(devConfig);

        SettingsImpl settings = new SettingsImpl(config);
        assertThat(settings.getCpuSetSize(), is(4));
    }

    @Test
    public void testNoCpuSetSize() {
        Config config = ConfigFactory.parseString("vampires.cpu-set-size= null").withFallback(devConfig);
        SettingsImpl settings = new SettingsImpl(config);

        assertThat(settings.getCpuSetSize(), is(1));
    }

    @Test
    public void testNoExecutors() {
        Config config = ConfigFactory.parseString("vampires.executors = null").withFallback(devConfig);
        SettingsImpl settings = new SettingsImpl(config);

        assertThat(settings.getExecutors().size(), is(1));
    }

    @Test
    public void getResources() {
        SettingsImpl settings = new SettingsImpl(ConfigFactory.load(CONF_FILE));
        assertThat(settings.getProviders().size(), not(0));

    }
}
