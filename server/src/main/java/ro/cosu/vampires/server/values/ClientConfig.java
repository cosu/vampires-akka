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

@AutoValue
public abstract class ClientConfig implements Serializable {

    private static String DEFAULT_EXECUTOR = "FORK";

    public static Builder withDefaults() {
        return builder().cpuSetSize(1).executor(DEFAULT_EXECUTOR);
    }

    public static Builder builder() {
        return new AutoValue_ClientConfig.Builder();
    }

    public static ClientConfig empty() {
        return builder()
                .cpuSetSize(1)
                .executor(DEFAULT_EXECUTOR)
                .numberOfExecutors(0)
                .build();
    }

    public abstract String executor();

    public abstract int cpuSetSize();

    public abstract int numberOfExecutors();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder executor(String executor);

        public abstract Builder cpuSetSize(int cpuSetSize);

        public abstract Builder numberOfExecutors(int cpuSetSize);

        public abstract ClientConfig build();
    }
}
