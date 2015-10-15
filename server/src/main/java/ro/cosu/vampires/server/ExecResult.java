package ro.cosu.vampires.server;


import autovalue.shaded.com.google.common.common.collect.ImmutableMap;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class ExecResult implements Serializable{
    private final List<String> output;
    private final int exitCode;
    private final String command;

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getStop() {
        return stop;
    }

    private final LocalDateTime start;
    private final LocalDateTime stop;
    private final long duration;

    private ImmutableMap<LocalDateTime, ImmutableMap<String, Double>> metrics;

    public ExecResult(Builder builder) {
        this.output = builder.output;
        this.exitCode = builder.exitCode;
        this.command = builder.command;
        this.start = builder.start;
        this.stop = builder.stop;
        this.duration = builder.duration;
    }

    @Override
    public String toString() {
        return "Result" + "["+Joiner.on(" ").
                join(ImmutableList.of(command, exitCode, start.toLocalTime(), stop.toLocalTime(), duration, output , metrics))
                +"]";
    }

    public void setMetrics(ImmutableMap<LocalDateTime, ImmutableMap<String, Double>> metrics) {
        this.metrics = metrics;
    }

    public int getExitCode() {
        return exitCode;
    }

    public List<String> getOutput() {
        return output;
    }

    public ImmutableMap<LocalDateTime, ImmutableMap<String, Double>> getMetrics() {
        return metrics;
    }

    public static class Builder {

        private LocalDateTime start;
        private long duration;
        private String command;
        private int exitCode;
        private List<String> output;
        private LocalDateTime stop;


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

        public Builder stop(LocalDateTime stop) {
            this.stop = stop;
            return this;
        }
    }
}
