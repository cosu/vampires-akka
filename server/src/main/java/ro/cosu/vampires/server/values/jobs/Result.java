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

package ro.cosu.vampires.server.values.jobs;

import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import ro.cosu.vampires.server.util.gson.AutoGson;
import ro.cosu.vampires.server.values.jobs.metrics.Trace;

@AutoValue
@AutoGson
public abstract class Result implements Serializable {

    public static Builder builder() {
        return new AutoValue_Result.Builder();
    }

    public static Result empty() {
        return new AutoValue_Result.Builder()
                .output(new LinkedList<>())
                .exitCode(-1)
                .trace(Trace.empty())
                .duration(0)
                .build();
    }

    public abstract List<String> output();

    public abstract int exitCode();

    public abstract long duration();

    public abstract Trace trace();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder output(List<String> output);

        public abstract Builder exitCode(int exitCode);

        public abstract Builder duration(long duration);

        public abstract Builder trace(Trace trace);

        public abstract Result build();


    }
}
