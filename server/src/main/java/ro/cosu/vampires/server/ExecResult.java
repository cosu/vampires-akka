package ro.cosu.vampires.server;


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class ExecResult implements Serializable{
    private final List<String> output;
    private final int exitCode;
    private final String command;
    private final LocalDateTime start;
    private final long duration;
    private  List<Double> loads;

    public ExecResult(Builder builder) {
        this.output = builder.output;
        this.exitCode = builder.exitCode;
        this.command = builder.command;
        this.start = builder.start;
        this.duration = builder.duration;
    }

    @Override
    public String toString() {
        return "Result" + "["+Joiner.on(" ").
                join(ImmutableList.of(command, exitCode, start.toLocalTime(), duration, output , loads))
                +"]";
    }

    public void setLoads(List<Double> loads) {
        this.loads = loads;
    }

    public int getExitCode() {
        return exitCode;
    }

    public List<String> getOutput() {
        return output;
    }

    public static class Builder {

        private LocalDateTime start;
        private long duration;
        private String command;
        private int exitCode;
        private List<String> output;


        public Builder duration(long duration){
            this.duration= duration;
            return this;
        }

        public Builder start(LocalDateTime start) {
            this.start = start;
            return this;
        }

        public Builder command (String command) {
            this.command = command;
            return this;
        }

        public Builder exitCode (int exitCode) {
            this.exitCode = exitCode;
            return this;
        }

        public Builder output (List<String> output) {
            this.output = output;
            return this;
        }

        public ExecResult build(){
            return new ExecResult(this);
        }
    }
}
