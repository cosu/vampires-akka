package ro.cosu.vampires.server.actors.messages.execution;

public class SubscribeExecution implements ExecutionMessage {
    public static SubscribeExecution create() {
        return new SubscribeExecution();
    }
}
