/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.actors;

import ro.cosu.vampires.server.resources.Resource;

import java.io.Serializable;

public class ResourceControl {

    public static class Shutdown implements Serializable {
    }

    public static class Up implements Serializable {
    }

    public static class Query implements Serializable {
        public final String resourceId;

        public Query(String resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public String toString() {
            return "Query{" +
                    "resourceId='" + resourceId + '\'' +
                    '}';
        }
    }

    public static class Create implements Serializable {
        public final Resource.Type type;
        public final Resource.Parameters parameters;

        public Create(Resource.Type type, Resource.Parameters parameters) {
            this.type = type;
            this.parameters = parameters;
        }

        @Override
        public String toString() {
            return "Create{" +
                    "type=" + type +
                    ", parameters=" + parameters +
                    '}';
        }
    }

    public static class Bootstrap {
        public final Resource.Type type;
        public final String name;

        public Bootstrap(Resource.Type type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Bootstrap{" +
                    "type=" + type +
                    ", name='" + name + '\'' +
                    '}';
        }
    }


}
