package ro.cosu.vampires.server.actors.messages;


import com.google.auto.value.AutoValue;

@AutoValue
public abstract class QueryResource {
    public static QueryResource create(String resourceId) {
        return new AutoValue_QueryResource(resourceId);
    }

    public abstract String resourceId();

}
