package ro.cosu.vampires.server.resources;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ResourceInfo {
    public abstract ResourceDescription description();
    public abstract Resource.Status status();
    public  static ResourceInfo  create(ResourceDescription description, Resource.Status status){
        return new AutoValue_ResourceInfo(description, status);
    }

}
