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

package ro.cosu.vampires.server.workload;

import com.google.common.collect.ImmutableList;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import ro.cosu.vampires.server.resources.Resource;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


public class ConfigurationTest {
    @Test
    public void builder() throws Exception {
        Configuration foo = Configuration.builder().description("foo")
                .resources(ImmutableList.of(
                        ResourceDemand.builder().count(1)
                                .resourceDescription(ResourceDescription.create("bar", Resource.ProviderType.MOCK, 0L))
                                .build()
                )).build();
        assertThat(foo.id(), not(isEmptyOrNullString()));

        assertThat(foo.resources().size(), not(0));
    }

    @Test
    public void fromConfig() throws Exception {
        String startConfig = "{" +
                "properties = foo\n" +
                "start  = [\n" +
                "    {\n" +
                "      provider = local\n" +
                "      type = local\n" +
                "      count = 1\n" +
                "    }\n" +
                "  ]" +
                "}";

        Config config = ConfigFactory.parseString(startConfig);

        ConfigurationPayload configuration = ConfigurationPayload.fromConfig(config);

        assertThat(configuration.resources().size(), is(1));
        assertThat(configuration.resources().get(0).resourceDescription().provider(), is(Resource.ProviderType.LOCAL));
    }


}