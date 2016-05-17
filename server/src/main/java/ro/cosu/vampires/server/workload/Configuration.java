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
import com.google.common.collect.ImmutableList;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class Configuration implements Id {

    //[
//        {
//        "id": "37ecfc16-f0fc-4c43-a915-bfef7ac6116b",
//        "created_at": "2016-08-05T08:40:51.620Z",
//        "lastupdate_at": "2016-08-05T08:40:51.620Z",
//        "properties" : "my optional properties",
//        "resources":
//        [
//        {
//        "provider": "ec2",
//        "providerType": "eu-west1.t2.micro",
//        "count": "5"
//        },
//        {
//        "provider": "ec2",
//        "providerType": "eu-west1.t2.nano",
//        "count": "5"
//        }
//        ],
//        "workload": "041363e3-6551-4202-947a-9fa8dab240ec",
//        "cost": 100
//        }
//        ]


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

    public Configuration touch() {
        return toBuilder().updatedAt(LocalDateTime.now()).build();
    }

    public abstract String id();

    public abstract LocalDateTime createdAt();

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


