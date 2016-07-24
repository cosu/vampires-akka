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

import com.codahale.metrics.Meter;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class MeterSnapshot {

    public static Builder builder() {
        return new AutoValue_MeterSnapshot.Builder();
    }

    public static MeterSnapshot fromMeter(String name, Meter meter) {

        return builder()
                .name(name)
                .count(meter.getCount())
                .m1(meter.getOneMinuteRate())
                .m5(meter.getFiveMinuteRate())
                .m15(meter.getFifteenMinuteRate())
                .mean(meter.getMeanRate())
                .build();

    }

    public abstract Builder toBuilder();

    public abstract String name();

    public abstract long count();

    public abstract double mean();

    public abstract double m1();

    public abstract double m5();

    public abstract double m15();


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long value);

        public abstract Builder name(String value);

        public abstract Builder mean(double value);

        public abstract Builder m1(double value);

        public abstract Builder m5(double value);

        public abstract Builder m15(double value);


        public abstract MeterSnapshot build();
    }

}
