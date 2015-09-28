package ro.cosu.vampires.server;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message {

    public static class Up implements  Serializable{}

    public static class Request implements Serializable {}

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
