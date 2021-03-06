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

package ro.cosu.vampires.server.values.jobs.metrics;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class Stats {
    public static Builder builder() {
        return new AutoValue_Stats.Builder();
    }

    public static Stats empty() {
        return builder().histograms(ImmutableMap.of())
                .values(ImmutableMap.of())
                .meters(ImmutableMap.of())
                .counters(ImmutableMap.of())
                .resources(ImmutableList.of())
                .build();
    }

    public abstract ImmutableList<ResourceInfo> resources();

    public abstract ImmutableMap<String, ValueSnapshot> values();

    public abstract ImmutableMap<String, CounterSnapshot> counters();

    public abstract ImmutableMap<String, MeterSnapshot> meters();

    public abstract ImmutableMap<String, HistogramSnapshot> histograms();


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Stats build();

        public abstract Builder resources(ImmutableList<ResourceInfo> resourceInfos);

        public abstract Builder values(ImmutableMap<String, ValueSnapshot> values);

        public abstract Builder meters(ImmutableMap<String, MeterSnapshot> meters);

        public abstract Builder histograms(ImmutableMap<String, HistogramSnapshot> histograms);

        public abstract Builder counters(ImmutableMap<String, CounterSnapshot> counters);


    }
}
