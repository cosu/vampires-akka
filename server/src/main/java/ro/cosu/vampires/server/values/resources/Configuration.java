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

package ro.cosu.vampires.server.values.resources;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import ro.cosu.vampires.server.util.gson.AutoGson;
import ro.cosu.vampires.server.values.Id;
import ro.cosu.vampires.server.values.jobs.ExecutionMode;

@AutoValue
@AutoGson
public abstract class Configuration implements Id {


    public static Builder builder() {
        return new AutoValue_Configuration.Builder()
                .id(UUID.randomUUID().toString())
                .description("")
                .cost(0.)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public static Configuration fromPayload(ConfigurationPayload payload) {
        return builder().description(payload.description())
                .resources(payload.resources()).build();
    }

    public static Configuration empty() {
        return builder().resources(ImmutableList.of()).build();
    }

    public Configuration updateFromPayload(ConfigurationPayload payload) {
        Builder builder = toBuilder();
        Optional.ofNullable(payload.description())
                .ifPresent(builder::description);
        Optional.ofNullable(payload.resources())
                .ifPresent(builder::resources);

        return builder.build();
    }

    public Configuration withResources(List<ResourceDemand> resourceDemands) {
        double cost = resourceDemands.stream().map(rd -> rd.resourceDescription().cost() * rd.count())
                .collect(Collectors.summingDouble(Double::doubleValue));
        return toBuilder().resources(ImmutableList.copyOf(resourceDemands)).cost(cost).build();
    }

    @Override
    public abstract String id();

    @Override
    public abstract LocalDateTime createdAt();

    @Override
    public abstract LocalDateTime updatedAt();

    public abstract String description();

    public abstract Double cost();

    public abstract ImmutableList<ResourceDemand> resources();

    public abstract Builder toBuilder();

    public Configuration withMode(ExecutionMode mode) {
        if (mode.equals(ExecutionMode.SAMPLE))
            return toBuilder().resources(
                    ImmutableList.copyOf(
                            resources().stream().map(r -> r.withCount(1)
                            ).collect(Collectors.toList()))).build();
        else return this;
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder id(String id);

        public abstract Builder resources(ImmutableList<ResourceDemand> resources);

        public abstract Builder description(String format);

        public abstract Builder createdAt(LocalDateTime createdAt);

        public abstract Builder updatedAt(LocalDateTime createdAt);

        public abstract Builder cost(Double cost);

        public abstract Configuration build();
    }

}


