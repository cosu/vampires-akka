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

package ro.cosu.vampires.server.resources.ssh;

import com.google.auto.value.AutoValue;

import com.typesafe.config.Config;

import java.util.UUID;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.values.resources.ResourceDescription;

@AutoValue

public abstract class SshResourceParameters implements Resource.Parameters {

    public static Builder builder() {
        return new AutoValue_SshResourceParameters.Builder().port(22)
                .id(UUID.randomUUID().toString())
                .serverId("");
    }

    @Override
    public abstract String command();

    public abstract String user();

    public abstract String address();

    public abstract String privateKey();

    public abstract int port();

    @Override
    public abstract String serverId();

    @Override
    public abstract String id();


    public abstract Builder toBuilder();

    @Override
    public SshResourceParameters withServerId(String serverId) {
        return toBuilder().serverId(serverId).build();
    }

    @Override
    public Resource.Parameters withId(String id) {
        return toBuilder().id(id).build();
    }

    @AutoValue.Builder
    public abstract static class Builder implements Resource.Parameters.Builder {

        @Override
        public Builder fromConfig(Config config) {
            this.command(config.getString("command"));
            this.user(config.getString("user"));
            this.address(config.getString("address"));
            this.privateKey(config.getString("privateKey"));
            if (config.hasPath("port")) {
                this.port(config.getInt("port"));
            }
            this.resourceDescription(
                    ResourceDescription.builder().resourceType(config.getString("type")).provider(Resource.ProviderType.SSH).build());
            return this;
        }


        public abstract Builder resourceDescription(ResourceDescription resourceDescription);

        public abstract Builder command(String s);

        public abstract Builder user(String s);

        public abstract Builder address(String s);

        public abstract Builder privateKey(String s);

        public abstract Builder port(int i);

        public abstract Builder serverId(String s);

        public abstract Builder id(String id);

        @Override
        public abstract SshResourceParameters build();

    }
}
