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

package ro.cosu.vampires.server.workload.estimators;

import com.google.common.collect.Maps;

import org.junit.Test;

import java.util.Map;
import java.util.stream.StreamSupport;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.values.resources.ResourceDescription;
import ro.cosu.vampires.server.values.resources.ConfigurationIterable;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ConfigurationIterableTest {
    @Test
    public void getNextSchedule() throws Exception {

        Map<ResourceDescription, Integer> resourceDescriptionLongMap = Maps.newHashMap();

        resourceDescriptionLongMap.put(ResourceDescription
                .create("small", Resource.ProviderType.MOCK, 10), 3);

        resourceDescriptionLongMap.put(ResourceDescription
                .create("medium", Resource.ProviderType.MOCK, 10), 3);

        resourceDescriptionLongMap.put(ResourceDescription
                .create("large", Resource.ProviderType.MOCK, 10), 3);

        ConfigurationIterable configurations = new ConfigurationIterable(resourceDescriptionLongMap);

        assertThat(StreamSupport.stream(configurations.spliterator(), false).count(), is(9L));

        configurations.forEach(c -> assertThat(c.resources().size() , not(0)));

    }
}