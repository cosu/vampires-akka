package ro.cosu.vampires.server.actors;

import java.io.Serializable;

import ro.cosu.vampires.server.resources.Resource;

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
