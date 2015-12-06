package ro.cosu.vampires.server.resources;


import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ResourceDescription {

    public abstract String id();
    public abstract Resource.Provider provider();
    public static ResourceDescription create(String id, Resource.Provider provider){
        return new AutoValue_ResourceDescription(id, provider);
    }

}
