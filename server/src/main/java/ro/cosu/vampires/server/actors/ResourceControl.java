package ro.cosu.vampires.server.actors;

import ro.cosu.vampires.server.resources.Resource;

import java.io.Serializable;

public class ResourceControl {

    public static class Shutdown implements Serializable {
    }

    public static class Up implements Serializable {
    }


    public static class Info implements Serializable {
    }



    public static class Create implements Serializable {
        final Resource.Provider provider;
        final Resource.Parameters parameters;

        public Create(Resource.Provider provider, Resource.Parameters parameters) {
            this.provider = provider;
            this.parameters = parameters;
        }

        @Override
        public String toString() {
            return "Create{" +
                    "provider=" + provider +
                    ", parameters=" + parameters +
                    '}';
        }
    }

    public static class Bootstrap {
        final Resource.Provider provider;
        final String name;

        public Bootstrap(Resource.Provider provider, String name) {
            this.provider = provider;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Bootstrap{" +
                    "provider=" + provider +
                    ", name='" + name + '\'' +
                    '}';
        }
    }


}
