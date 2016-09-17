package ro.cosu.vampires.server.actors.messages.execution;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;

import ro.cosu.vampires.server.values.jobs.Execution;

@AutoValue
public abstract class ResponseExecution implements ExecutionMessage {
    public static ResponseExecution create(List<Execution> executions) {
        return new AutoValue_ResponseExecution(ImmutableList.copyOf(executions));
    }

    public abstract List<Execution> values();
}
