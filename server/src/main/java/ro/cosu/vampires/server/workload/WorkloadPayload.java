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

import com.typesafe.config.Config;

import javax.annotation.Nullable;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class WorkloadPayload {

    public static Builder builder() {
        return new AutoValue_WorkloadPayload.Builder()
                .format("%s")
                .url("")
                .sequenceStart(0)
                .sequenceStop(0);
    }

    public static WorkloadPayload fromConfig(Config config) {
        String format = "";
        if (config.hasPath("format")) {
            format = config.getString("format");
        }

        String url = "";
        if (config.hasPath("url")) {
            url = config.getString("url");
        }

        String description = "";
        if (config.hasPath("description")) {
            url = config.getString("description");
        }

        int sequenceStart = config.getInt("sequenceStart");
        int sequenceStop = config.getInt("sequenceStop");
        String task = config.getString("task");

        return builder().format(format)
                .task(task)
                .url(url)
                .description(description)
                .sequenceStart(sequenceStart)
                .sequenceStop(sequenceStop)
                .build();
    }

    @Nullable
    public abstract String id();

    public abstract int sequenceStart();

    public abstract int sequenceStop();

    @Nullable
    public abstract String task();

    @Nullable
    public abstract String format();

    @Nullable
    public abstract String url();

    @Nullable
    public abstract String description();

    @Nullable
    public abstract String file();


    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder sequenceStart(int sequenceStrat);

        public abstract Builder sequenceStop(int sequenceStop);

        public abstract Builder task(String task);

        public abstract Builder format(String format);

        public abstract Builder url(String format);

        public abstract Builder description(String format);

        public abstract Builder file(String id);

        public abstract WorkloadPayload build();
    }

}
