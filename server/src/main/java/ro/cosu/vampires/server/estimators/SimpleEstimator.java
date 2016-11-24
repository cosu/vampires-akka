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

import java.util.Map;

import ro.cosu.vampires.server.values.resources.Configuration;
import ro.cosu.vampires.server.values.resources.ResourceDescription;

public class SimpleEstimator implements Estimator {

    private final Map<ResourceDescription, Double> durationsPerInstanceType;

    private final double numberOfJobs;

    public SimpleEstimator(Map<ResourceDescription, Double> durationsPerInstanceType,
                           double numberOfJobs) {
        this.durationsPerInstanceType = durationsPerInstanceType;
        this.numberOfJobs = numberOfJobs;
    }

    public double estimate(Configuration configuration) {

        double ratesSum = configuration.resources().stream()
                .filter(rd -> rd.count() > 0)
                .mapToDouble(rd -> {
                    int count = rd.count();
                    Double duration = durationsPerInstanceType.get(rd.resourceDescription());
                    return count / duration;
                }).sum();

        return numberOfJobs / ratesSum;
    }
}