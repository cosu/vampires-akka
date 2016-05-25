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

package ro.cosu.vampires.server.resources.das5;

import com.google.auto.value.AutoValue;

import com.typesafe.config.Config;

import java.util.UUID;

import ro.cosu.vampires.server.resources.Resource;

@AutoValue

public abstract class Das5ResourceParameters implements Resource.Parameters {

    public static Builder builder() {
        return new AutoValue_Das5ResourceParameters.Builder()
                .port(22).providerType(Resource.ProviderType.DAS5)
                .cost(0.)
                .id(UUID.randomUUID().toString())
                .serverId("");
    }

    public abstract String command();

    public abstract String user();

    public abstract String address();

    public abstract String privateKey();

    public abstract int port();

    public abstract double cost();

    public abstract Resource.ProviderType providerType();

    public abstract String serverId();

    public abstract Builder toBuilder();

    public abstract String id();

    public abstract String instanceType();


    public Das5ResourceParameters withServerId(String serverId) {
        return toBuilder().serverId(serverId).build();
    }

    public Resource.Parameters withId(String id) {
        return toBuilder().id(id).build();
    }

    @AutoValue.Builder
    public abstract static class Builder implements Resource.Parameters.Builder {

        public abstract Builder command(String s);

        public abstract Builder user(String s);

        public abstract Builder address(String s);

        public abstract Builder privateKey(String s);

        public abstract Builder port(int i);

        public abstract Builder cost(double cost);

        public abstract Builder providerType(Resource.ProviderType providerType);

        public abstract Builder serverId(String s);

        public abstract Builder instanceType(String instanceType);

        public Builder fromConfig(Config config) {
            this.command(config.getString("command"));
            this.user(config.getString("user"));
            this.cost(config.getDouble("cost"));
            this.address(config.getString("address"));
            this.privateKey(config.getString("privateKey"));
            if (config.hasPath("port")) {
                this.port(config.getInt("port"));
            }
            return this;
        }

        public abstract Builder id(String id);

        public abstract Das5ResourceParameters build();

    }
}
