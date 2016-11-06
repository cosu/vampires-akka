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
import com.google.common.base.Splitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ro.cosu.vampires.server.util.gson.AutoGson;
import ro.cosu.vampires.server.values.AutoValueUtil;
import ro.cosu.vampires.server.values.Id;

@AutoValue
@AutoGson

public abstract class Workload implements Id {

    public static Workload.Builder builder() {
        return new AutoValue_Workload.Builder().id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .format("")
                .task("")
                .description("")
                .url("")
                .file("")
                .sequenceStart(0)
                .sequenceStop(0);
    }


    public static Workload fromPayload(WorkloadPayload payload) {
        return new AutoValueUtil<WorkloadPayload, Builder>() {
        }
                .builderFromPayload(payload, builder()).build();
    }


    public Workload updateFromPayload(WorkloadPayload payload) {
        return new AutoValueUtil<WorkloadPayload, Workload.Builder>() {
        }
                .builderFromPayload(payload, toBuilder()).build();
    }


    @Override
    public abstract String id();

    @Override
    public abstract LocalDateTime createdAt();

    @Override
    public abstract LocalDateTime updatedAt();

    public abstract int sequenceStart();

    public abstract int sequenceStop();

    public abstract String task();

    public abstract String format();

    public abstract String url();

    public abstract String file();

    public abstract String description();

    public abstract Builder toBuilder();

    public Builder update() {
        return toBuilder()
                .updatedAt(LocalDateTime.now());
    }

    public int size() {
        return jobs().size();
    }

    public List<Job> jobs() {
        if (!file().equals("")) {
            return getJobsFromFile();
        } else {
            return getJobsFromSequence();
        }
    }


    private List<Job> getJobsFromFile() {
        return Splitter.on("\n").splitToList(file()).stream()
                .map(Job.empty()::withCommand)
                .collect(Collectors.toList());
    }

    private List<Job> getJobsFromSequence() {
        final String finalUrl = url();
        final String finalFormat = format();
        return IntStream.rangeClosed(sequenceStart(), sequenceStop()).mapToObj(i -> String.format(finalFormat, i))
                .map(f -> String.format("%s %s%s", task(), finalUrl, f).trim())
                .map(command -> Job.empty().withCommand(command))
                .collect(Collectors.toList());
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder id(String id);

        public abstract Builder sequenceStart(int sequenceStrat);

        public abstract Builder createdAt(LocalDateTime createdAt);

        public abstract Builder updatedAt(LocalDateTime createdAt);

        public abstract Builder sequenceStop(int sequenceStop);

        public abstract Builder task(String task);

        public abstract Builder format(String format);

        public abstract Builder file(String file);

        public abstract Builder url(String format);

        public abstract Builder description(String description);

        public abstract Workload build();
    }
}
