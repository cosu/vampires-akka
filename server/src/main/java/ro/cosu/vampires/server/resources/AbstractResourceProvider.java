/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.resources;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;

public abstract class AbstractResourceProvider implements ResourceProvider {

    private Config config;

    @Override
    public Config getConfig() {
        return config;
    }

    @Inject
    public void setConfig(@Named("Config") Config config) {
        this.config = config;
    }

    protected Config getConfigForInstance(String instanceName) {
        Config appDefaults = getConfig().getConfig("resources");
        Config providerDefaults = getConfig().getConfig("resources." + getType().toString().toLowerCase());

        return getSimpleConfigForInstance(instanceName)
                .withFallback(providerDefaults)
                .withFallback(appDefaults);
    }

    protected Config getSimpleConfigForInstance(String instanceName) {
        return getConfig().getConfig(getInstanceKey(instanceName));
    }

    protected String getInstanceKey(String instanceName) {
        return "resources." + getType().toString().toLowerCase() + "." + instanceName.toLowerCase();
    }

    @Override
    public Resource.Parameters getParameters(String instanceName) {
        return getBuilder().fromConfig(getConfigForInstance(instanceName)).build();
    }


}
