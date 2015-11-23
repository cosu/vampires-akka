package ro.cosu.vampires.server.actors;

import ro.cosu.vampires.server.resources.Resource;

import java.io.Serializable;

public class ResourceControl {

    public static class Shutdown implements Serializable {
    }

    public static class Up implements Serializable {
    }

    public static class Destroy implements Serializable {
    }

    public static class Info implements Serializable {
    }


    public static class Create implements Serializable {
        final Resource.Type type;
        final Resource.Parameters parameters;

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

    public static class Start {
        final Resource.Type type;
        final String name;

        public Start(Resource.Type type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Start{" +
                    "type=" + type +
                    ", name='" + name + '\'' +
                    '}';
        }
    }


}