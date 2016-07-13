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

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.workload.ResourceDescription;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class SimpleEstimatorTest {
    @Test
    public void estimate() throws Exception {

        Map<ResourceDescription, Long> counts = Maps.newHashMap();
        Map<ResourceDescription, Double> durations = Maps.newHashMap();

        ResourceDescription resourceDescription = ResourceDescription.create("local", Resource.ProviderType.MOCK, 100);

        counts.put(resourceDescription, 1L);
        durations.put(resourceDescription, 100.);


        SimpleEstimator simpleEstimator = new SimpleEstimator(counts, durations, 100.);

        assertThat(simpleEstimator.estimate(), not(0));

    }

}