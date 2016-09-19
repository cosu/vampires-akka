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
import com.google.common.collect.Sets;

import java.time.LocalDateTime;
import java.util.HashSet;

import ro.cosu.vampires.server.util.gson.AutoGson;
import ro.cosu.vampires.server.values.jobs.metrics.Stats;

@AutoValue
@AutoGson
public abstract class ExecutionInfo {

    private static HashSet<Status> ACTIVE_STATUSES =
            Sets.newHashSet(ExecutionInfo.Status.RUNNING, ExecutionInfo.Status.STARTING);

    public static Builder builder() {
        return new AutoValue_ExecutionInfo.Builder()
                .updatedAt(LocalDateTime.now());
    }

    public static ExecutionInfo empty() {
        return builder()
                .total(0)
                .completed(0)
                .remaining(0)
                .failed(0)
                .elapsed(0)
                .stats(Stats.empty())
                .status(Status.STARTING)
                .build();
    }


    public abstract Status status();

    public abstract LocalDateTime updatedAt();

    public abstract int failed();

    public abstract int remaining();

    public abstract int total();

    public abstract int completed();

    public abstract long elapsed();

    public abstract Stats stats();

    public abstract Builder toBuilder();

    private Builder update() {
        return toBuilder().updatedAt(LocalDateTime.now());
    }

    public ExecutionInfo updateCompleted(int completed) {
        return update().completed(completed)
                .build();
    }

    public ExecutionInfo updateTotal(int total) {
        return update().total(total).build();
    }

    public ExecutionInfo updateRemaining(int remaining) {
        return update().remaining(remaining).build();
    }

    public ExecutionInfo updateElapsed(long elapsed) {
        return update().elapsed(elapsed).build();
    }

    public ExecutionInfo updateFailed(int failed) {
        return update().failed(failed).build();
    }

    public ExecutionInfo updateStatus(Status status) {
        return update().status(status).build();
    }

    public ExecutionInfo updateStats(Stats stats) {
        return update().stats(stats).build();
    }


    public enum Status {
        STARTING,
        RUNNING,
        CANCELED,
        STOPPING,
        FAILED,
        FINISHED;

        private static HashSet<Status> ACTIVE_STATUSES =
                Sets.newHashSet(ExecutionInfo.Status.RUNNING, ExecutionInfo.Status.STARTING);

        public boolean isActiveStatus() {
            return ACTIVE_STATUSES.contains(this);
        }

    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder status(Status status);

        public abstract Builder updatedAt(LocalDateTime createdAt);

        public abstract Builder total(int i);

        public abstract Builder stats(Stats stats);


        public abstract Builder completed(int i);

        public abstract Builder elapsed(long i);

        public abstract Builder remaining(int i);

        public abstract Builder failed(int i);

        public abstract ExecutionInfo build();


    }

}
