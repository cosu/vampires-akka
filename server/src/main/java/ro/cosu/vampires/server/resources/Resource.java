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

package ro.cosu.vampires.server.resources;

import com.typesafe.config.Config;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import ro.cosu.vampires.server.values.resources.ResourceDescription;

public interface Resource {

    void connected();

    CompletableFuture<Resource> start();

    CompletableFuture<Resource> stop();

    void onStart() throws Exception;

    void onStop() throws Exception;

    void onFail() throws Exception;

    Parameters parameters();

    Status status();

    default ResourceInfo info() {
        return ResourceInfo.create(parameters(), status());
    }

    enum Status {
        SLEEPING,
        STARTING,
        RUNNING,
        FAILED,
        STOPPING,
        STOPPED,
        UNKNOWN, CONNECTED,
    }

    enum ProviderType {
        SSH,
        LOCAL,
        DAS5,
        EC2,
        MOCK
    }

    interface Parameters extends Serializable {

        ResourceDescription resourceDescription();

        String id();

        String serverId();

        String command();

        Parameters withServerId(String serverId);

        Parameters withId(String id);

        interface Builder {

            Builder fromConfig(Config config);

            Resource.Parameters build();
        }
    }

}
