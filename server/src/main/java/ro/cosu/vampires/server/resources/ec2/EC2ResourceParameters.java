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

package ro.cosu.vampires.server.resources.ec2;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

import com.typesafe.config.Config;

import java.util.UUID;

import ro.cosu.vampires.server.resources.Resource;

@AutoValue
public abstract class EC2ResourceParameters implements Resource.Parameters {

    public static Builder builder() {
        return new AutoValue_EC2ResourceParameters.Builder().providerType(Resource.ProviderType.EC2)
                .id(UUID.randomUUID().toString())
                .serverId("");
    }

    public abstract String command();

    public abstract String imageId();

    public abstract String instanceType();

    public abstract String keyName();

    public abstract String region();

    public abstract String securityGroup();

    public abstract Resource.ProviderType providerType();

    public abstract String serverId();

    public abstract String id();

    public abstract double cost();

    public abstract Builder toBuilder();

    public EC2ResourceParameters withServerId(String serverId) {
        return toBuilder().serverId(serverId).build();
    }

    public Resource.Parameters withId(String id) {
        return toBuilder().id(id).build();
    }

    @AutoValue.Builder
    public abstract static class Builder implements Resource.Parameters.Builder {

        public abstract Builder command(String s);

        public abstract Builder imageId(String s);

        public abstract Builder cost(double cost);

        public abstract Builder id(String id);

        public abstract Builder instanceType(String s);

        public abstract Builder keyName(String s);

        public abstract Builder region(String s);

        public abstract Builder securityGroup(String s);

        public abstract Builder serverId(String s);

        public abstract Builder providerType(Resource.ProviderType providerType);

        public Builder fromConfig(Config config) {
            String region = config.getString("region");
            Preconditions.checkArgument(region.split("-").length == 3, "Invalid region: " + region);
            this.cost(config.getDouble("cost"));
            this.command(config.getString("command"));
            this.instanceType(config.getString("instanceType"));
            this.imageId(config.getString("imageId"));
            this.keyName(config.getString("keyName"));
            this.region(region);
            this.securityGroup(config.getString("securityGroup"));
            return this;
        }


        public abstract EC2ResourceParameters build();
    }
}
