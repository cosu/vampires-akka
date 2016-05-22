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

import com.google.auto.value.AutoValue;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class HistogramSnapshot {

    public static Builder builder() {
        return new AutoValue_HistogramSnapshot.Builder();
    }

    public static HistogramSnapshot fromHistogram(String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();

        return builder()
                .name(name)
                .count(histogram.getCount())
                .max(snapshot.getMax())
                .min(snapshot.getMin())
                .mean(snapshot.getMean())
                .p50(snapshot.getMedian())
                .p75(snapshot.get75thPercentile())
                .p95(snapshot.get95thPercentile())
                .p98(snapshot.get98thPercentile())
                .p99(snapshot.get99thPercentile())
                .p999(snapshot.get999thPercentile())
                .stddev(snapshot.getStdDev())
                .build();

    }

    public abstract Builder toBuilder();

    public abstract String name();

    public abstract long count();

    public abstract long max();

    public abstract long min();

    public abstract double mean();

    public abstract double p50();

    public abstract double p75();

    public abstract double p95();

    public abstract double p98();

    public abstract double p99();

    public abstract double p999();

    public abstract double stddev();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long value);

        public abstract Builder name(String value);

        public abstract Builder max(long value);

        public abstract Builder min(long value);

        public abstract Builder mean(double value);

        public abstract Builder p50(double value);

        public abstract Builder p75(double value);

        public abstract Builder p95(double value);

        public abstract Builder p98(double value);

        public abstract Builder p99(double value);

        public abstract Builder p999(double value);

        public abstract Builder stddev(double value);

        public abstract HistogramSnapshot build();
    }


}
