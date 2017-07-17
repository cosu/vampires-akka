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

package ro.cosu.vampires.server.values;

import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;

import ro.cosu.vampires.server.util.gson.AutoGson;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;

@AutoValue
@AutoGson

public abstract class ClientInfo implements Serializable {

    public static Builder builder() {
        return new AutoValue_ClientInfo.Builder().start(ZonedDateTime.now());
    }

    public abstract String id();

    public abstract ZonedDateTime start();

    public abstract Map<String, Integer> executors();

    public abstract Metrics metrics();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder start(ZonedDateTime start);

        public abstract Builder executors(Map<String, Integer> executors);

        public abstract Builder metrics(Metrics metrics);

        public abstract Builder id(String id);

        public abstract ClientInfo build();


    }
}
