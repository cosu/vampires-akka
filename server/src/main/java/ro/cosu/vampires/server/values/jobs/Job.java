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
import java.time.LocalDateTime;
import java.util.UUID;

import ro.cosu.vampires.server.util.gson.AutoGson;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;

@AutoValue
@AutoGson
public abstract class Job implements Serializable {


    public static Job empty() {
        return builder().computation(Computation.empty())
                .hostMetrics(Metrics.empty())
                .result(Result.empty()).build();
    }

    public static Job backoff(int backoffInterval) {
        return builder().computation(Computation.backoff(backoffInterval))
                .hostMetrics(Metrics.empty())
                .result(Result.empty()).build();
    }

    public static Builder builder() {
        return new AutoValue_Job.Builder()
                .created(LocalDateTime.now())
                .id(UUID.randomUUID().toString())
                .from("")
                .status(JobStatus.NEW);
    }

    public abstract String id();

    public abstract String from();

    public abstract LocalDateTime created();

    public abstract JobStatus status();

    public abstract Computation computation();

    public abstract Result result();

    public abstract Metrics hostMetrics();

    public abstract Builder toBuilder();

    public Job withCommand(String command) {
        return toBuilder().computation(Computation.withCommand(command)).build();
    }

    public Job from(String from) {
        return toBuilder()
                .from(from)
                .build();
    }

    public Job withHostMetrics(Metrics metrics) {
        return toBuilder().hostMetrics(metrics)
                .status(JobStatus.COMPLETE)
                .build();
    }

    public Job withResult(Result result) {
        return toBuilder().result(result)
                .status(JobStatus.EXECUTED)
                .build();
    }

    public Job withComputation(Computation computation) {
        return toBuilder().computation(computation).build();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder computation(Computation computation);

        public abstract Builder result(Result result);

        public abstract Builder hostMetrics(Metrics metrics);

        public abstract Builder created(LocalDateTime created);

        public abstract Builder from(String from);

        public abstract Builder status(JobStatus jobstatus);

        public abstract Builder id(String id);

        public abstract Job build();
    }
}
