package ro.cosu.vampires.server;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceDescription;

import java.io.Serializable;

public class Message {

    public static class Shutdown {
    }

    public static class Up implements  Serializable{}

    public static class Request implements Serializable {}

    public static class CreateResource implements Serializable {
        final Resource.Type type;
        final Resource.Parameters  parameters;

        public CreateResource(Resource.Type type, Resource.Parameters parameters) {
            this.type = type;
            this.parameters = parameters;
        }
    }

    public static class NewResource {
        final Resource.Type type;
        final String name;

        public NewResource(Resource.Type type, String name) {
            this.type = type;
            this.name = name;
        }
    }


    public static class DestroyResource implements Serializable  {
        final ResourceDescription resourceDescription;

        public DestroyResource(ResourceDescription resourceDescription) {
            this.resourceDescription = resourceDescription;
        }
    }

    public static class GetResourceInfo implements Serializable  {
        final ResourceDescription resourceDescription;

        public GetResourceInfo(ResourceDescription resourceDescription) {
            this.resourceDescription = resourceDescription;
        }
    }





}
