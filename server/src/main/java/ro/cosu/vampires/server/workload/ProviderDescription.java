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

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson

public abstract class ProviderDescription {
//    "provider": "ec2",
//            "name" : "Amazon EC2",
//            "resources": [
//    {
//        "type": "eu-west1.t2.nano",
//            "cost": 10
//    }, {
//        "type": "eu-west1.t2.micro",
//                "cost": 100
//    }, {
//        "type": "eu-west1.t3.micro",
//                "cost": 110
//    }
//},

    public static Builder builder() {
        return new AutoValue_ProviderDescription.Builder();
    }

    public abstract Resource.ProviderType provider();

    public abstract String description();

    public abstract ImmutableList<ResourceDescription> resources();

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder provider(Resource.ProviderType provider);

        public abstract Builder description(String name);

        public abstract Builder resources(ImmutableList<ResourceDescription> resourceDescriptions);

        public abstract ProviderDescription build();
    }
}
