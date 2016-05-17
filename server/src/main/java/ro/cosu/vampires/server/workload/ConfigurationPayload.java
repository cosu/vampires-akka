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
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;

import com.typesafe.config.Config;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class ConfigurationPayload {

    public static ConfigurationPayload fromConfig(Config config) {

        String description = config.hasPath("properties") ? config.getString("properties") : "";

        List<ResourceDemand> resourceDemandsList = config.getConfigList("start")
                .stream().map(demandConfig -> {
                    String type = demandConfig.getString("type");
                    int count = demandConfig.getInt("count");

                    Resource.ProviderType providerType = Enums.stringConverter
                            (Resource.ProviderType.class).convert(demandConfig.getString("provider").toUpperCase());
                    return ResourceDemand.builder().count(count).type(type)
                            .provider(providerType).build();
                }).collect(Collectors.toList());

        ImmutableList<ResourceDemand> resourceDemands = ImmutableList.copyOf(resourceDemandsList);

        return create(description, resourceDemands);
    }

    public static ConfigurationPayload create(String description, ImmutableList<ResourceDemand> resourceDemands) {
        return builder().description(description).resources(resourceDemands).build();
    }

    public static Builder builder() {
        return new AutoValue_ConfigurationPayload.Builder();
    }

    @Nullable
    public abstract String id();

    @Nullable
    public abstract String description();

    @Nullable
    public abstract ImmutableList<ResourceDemand> resources();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder id(String id);

        public abstract Builder description(String description);

        public abstract Builder resources(ImmutableList<ResourceDemand> resources);

        public abstract ConfigurationPayload build();
    }

}
