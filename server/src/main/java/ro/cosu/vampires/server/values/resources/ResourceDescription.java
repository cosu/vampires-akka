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

import java.time.LocalDateTime;
import java.util.UUID;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.util.gson.AutoGson;
import ro.cosu.vampires.server.values.Id;

@AutoValue
@AutoGson

public abstract class ResourceDescription implements Id {

    public static Builder builder() {
        return new AutoValue_ResourceDescription.Builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public abstract String type();

    public abstract Resource.ProviderType provider();

    public abstract double cost();

    public abstract String id();

    public abstract LocalDateTime createdAt();

    public abstract LocalDateTime updatedAt();

    @AutoValue.Builder
    public static abstract class Builder {

        public abstract Builder provider(Resource.ProviderType provider);

        abstract Resource.ProviderType provider();

        public abstract Builder type(String type);

        abstract String type();

        public abstract Builder cost(double cost);

        public abstract Builder id(String id);

        public abstract Builder createdAt(LocalDateTime createdAt);

        public abstract Builder updatedAt(LocalDateTime createdAt);

        abstract ResourceDescription autoBuild();

        public ResourceDescription build() {
            String result = UUID.nameUUIDFromBytes((provider().toString() + type()).getBytes()).toString();
            id(result);
            return autoBuild();
        }
    }

}
