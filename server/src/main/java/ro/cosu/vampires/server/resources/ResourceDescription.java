package ro.cosu.vampires.server.resources;


import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ResourceDescription {

    public static ResourceDescription create(String id, Resource.Type type) {
        return new AutoValue_ResourceDescription(id, type);
    }

    public static ResourceDescription empty(Resource.Type type) {
        return new AutoValue_ResourceDescription("", type);
    }

    public abstract String id();

    public abstract Resource.Type provider();

}
