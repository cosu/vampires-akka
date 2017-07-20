package ro.cosu.vampires.server.actors.messages.resource;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StopResource {
    public static StopResource create(String id) {
        return new AutoValue_StopResource(id);
    }

    public abstract String id();
}
