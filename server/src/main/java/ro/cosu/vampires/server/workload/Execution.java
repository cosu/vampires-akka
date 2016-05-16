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

import java.time.LocalDateTime;
import java.util.UUID;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class Execution implements Id {

    public static Builder builder() {
        return new AutoValue_Execution.Builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .id(UUID.randomUUID().toString());
    }

    public abstract String id();

    public abstract Configuration configuration();

    public abstract Workload workload();

    public abstract ExecutionMode type();

    public abstract ExecutionInfo info();

    public abstract LocalDateTime createdAt();

    public abstract LocalDateTime updatedAt();

    public  abstract Builder toBuilder();

    public Execution withInfo(ExecutionInfo info) {
        return toBuilder()
                .updatedAt(LocalDateTime.now())
                .info(info).build();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder configuration(Configuration configuration);

        public abstract Builder workload(Workload workload);

        public abstract Builder type(ExecutionMode type);

        public abstract Builder id(String id);

        public abstract Builder info(ExecutionInfo status);

        public abstract Builder createdAt(LocalDateTime createdAt);

        public abstract Builder updatedAt(LocalDateTime createdAt);

        public abstract Execution build();

    }


}
