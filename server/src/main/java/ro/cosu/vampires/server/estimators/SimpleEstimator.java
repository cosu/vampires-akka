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

import com.google.common.collect.HashBasedTable;

import ro.cosu.vampires.server.actors.execution.StatsProcessor;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.values.resources.Configuration;

public class SimpleEstimator implements Estimator {


    private StatsProcessor statsProcessor;

    public SimpleEstimator(StatsProcessor statsProcessor) {
        this.statsProcessor = statsProcessor;

        HashBasedTable<String, Resource.ProviderType, Long> counts = HashBasedTable.create();
    }


    public double estimate(Configuration configuration, double numberOfJobs) {

        return 0;
        /*
        double ratesSum = configuration.resources().stream()
                .filter(rd -> rd.count() > 0)
                .mapToDouble(rd -> {
                    int count = rd.count();
                    Double duration = durationsPerInstanceType.get(rd.resourceDescription());
                    return count / duration;
                }).sum();

        return numberOfJobs / ratesSum;
        */
    }
}
