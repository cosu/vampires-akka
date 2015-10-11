package ro.cosu.vampires.server;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceDescription;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message {

    public static class Up implements  Serializable{}

    public static class Request implements Serializable {}

    public static class CreateResource implements Serializable {
        final Resource.Type type;

        public CreateResource(Resource.Type type) {
            this.type = type;
        }
    }

    public static class DestroyResource implements Serializable  {
        final ResourceDescription resourceDescription;

        public DestroyResource(ResourceDescription resourceDescription) {
            this.resourceDescription = resourceDescription;
        }
    }

    public static class GetResourceDescription implements Serializable  {
        final ResourceDescription resourceDescription;

        public GetResourceDescription(ResourceDescription resourceDescription) {
            this.resourceDescription = resourceDescription;
        }
    }




    public static class Computation implements Serializable{
        final String command;
        final LocalDateTime created = LocalDateTime.now();

        public Computation(String command){
            this.command =  command;
        }

        public String getCommand() {
            return command;
        }

        public LocalDateTime getCreated() {
            return created;
        }

        @Override
        public String toString() {
            return "Computation [" +this.getCommand() + ":" + getCreated() +"]";
        }
    }
    public static class Result implements Serializable{
        public ExecResult getResult() {
            return result;
        }

        final ExecResult result;
        final Computation computation;


        public Result(ExecResult result, Computation computation) {
            this.result = result;
            this.computation = computation;
        }

        public Computation getComputation() {
            return computation;
        }

        @Override
        public String toString() {
            return "Result[" + computation.toString() + "->" + result.toString() + "]";
        }
    }
}
