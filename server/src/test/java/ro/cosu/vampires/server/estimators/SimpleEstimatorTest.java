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

package ro.cosu.vampires.server.estimators;

public class SimpleEstimatorTest {
/*G
    @Test
    public void estimateOneResource() throws Exception {

        Map<ResourceDescription, Double> durations = Maps.newHashMap();

        List<ResourceDemand> demand = Collections.singletonList(ResourceDemand.builder().count(1).resourceDescription(
                ResourceDescription.builder().provider(Resource.ProviderType.MOCK).resourceType("small").cost(10).build()
                ).build()
        );

        demand.forEach(r -> durations.put(r.resourceDescription(), 1.));

        Configuration configuration = Configuration.builder().resources(ImmutableList.copyOf(demand)).build();

        SimpleEstimator simpleEstimator = new SimpleEstimator(durations, 1);

        assertThat(simpleEstimator.estimate(configuration), is(1.));
    }


    @Test
    public void estimateTwoJobs() throws Exception {

        Map<ResourceDescription, Double> durations = Maps.newHashMap();

        List<ResourceDemand> demand = Collections.singletonList(ResourceDemand.builder().count(1).resourceDescription(
                ResourceDescription.builder().provider(Resource.ProviderType.MOCK).type("small").cost(10).build()
                ).build()
        );

        demand.forEach(r -> durations.put(r.resourceDescription(), 1.));

        SimpleEstimator simpleEstimator = new SimpleEstimator(durations, 2);

        Configuration configuration = Configuration.builder().resources(ImmutableList.copyOf(demand)).build();

        assertThat(simpleEstimator.estimate(configuration), is(2.));
    }

    @Test
    public void estimateTwoResources() throws Exception {

        Map<ResourceDescription, Double> durations = Maps.newHashMap();

        List<ResourceDemand> demand = Collections.singletonList(ResourceDemand.builder().count(2).resourceDescription(
                ResourceDescription.builder().provider(Resource.ProviderType.MOCK).resourceType("small").cost(10).build()
                ).build()
        );

        demand.forEach(r -> durations.put(r.resourceDescription(), 1.));

        SimpleEstimator simpleEstimator = new SimpleEstimator(durations, 2);

        Configuration configuration = Configuration.builder().resources(ImmutableList.copyOf(demand)).build();

        assertThat(simpleEstimator.estimate(configuration), is(1.));
    }


    @Test
    public void estimateMoreResources() throws Exception {

        Map<ResourceDescription, Double> durations = Maps.newHashMap();

        List<ResourceDemand> demand = Collections.singletonList(ResourceDemand.builder().count(50).resourceDescription(
                ResourceDescription.builder().provider(Resource.ProviderType.MOCK).type("small").cost(10).build()
                ).build()
        );

        demand.forEach(r -> durations.put(r.resourceDescription(), 1.));

        SimpleEstimator simpleEstimator = new SimpleEstimator(durations, 100);

        Configuration configuration = Configuration.builder().resources(ImmutableList.copyOf(demand)).build();

        assertThat(simpleEstimator.estimate(configuration), is(2.));
    }
    */
}